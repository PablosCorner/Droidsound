
#include "io68/default.h"
#include "file68/sc68/file68_vfs.h" /* Need vfs68.h before sc68.h */

/* libsc68 includes */
#include "libsc68/sc68/sc68.h"
#include "libsc68/sc68/mixer68.h"
#include "libsc68/sc68/conf68.h"

#ifndef HAVE_BASENAME
#include "libc68.h"
#endif

/* libsc68 emulators includes */
#include "emu68/emu68.h"
#include "emu68/excep68.h"
#include "emu68/ioplug68.h"
#include "io68/io68.h"

/* file68 includes */
#include "file68/sc68/file68.h"
#include "file68/sc68/file68_err.h"
#include "file68/sc68/file68_str.h"
#include "file68/sc68/file68_uri.h"
#include "file68/sc68/file68_rsc.h"
#include "file68/sc68/file68_msg.h"
#include "file68/sc68/file68_opt.h"

/* stardard includes */
#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include <assert.h>
#include <errno.h>

#ifdef HAVE_LIBGEN_H
#include <libgen.h>
#endif

#define MK4CC(A,B,C,D) (((int)(A)<<24)|((int)(B)<<16)|((int)(C)<<8)|((int)(D)))

/* Bunch of constant use by sc68
 */
enum {
  /* Internal use for get_pos() and set_pos() */
  SC68_POS_PLAY, SC68_POS_TRACK,  SC68_POS_DISK,
  /* Exception handler to catch badly initialized exceptions */
  INTR_ADDR = 0x800,
  /* TOS emulator */
  TRAP_ADDR = 0x1000,
  /* # of instructions for music play code */
  TRAP_MAX_INST = 10000u,
  /* # of instructions for music play code */
  PLAY_MAX_INST = 1000000u,
  /* # of instructions for music init code */
  INIT_MAX_INST = 10000000u,
  /* default music time in seconds  */
  TIME_DEF = 3 * 60,
  /* sc68_t magic identifier value. */
  SC68_MAGIC = MK4CC('s','c','6','8'),
  /* disk68_t magic identifier value. */
  DISK_MAGIC = SC68_DISK_ID,
  /* Error message maximum length */
  ERRMAX = 96,
  /* Default amiga blend */
  AGA_BLEND = 0x50,
  /* Option "force-loop" off */
  CFG_LOOP_OFF = 0,
  /* Option "force-loop" infinite */
  CFG_LOOP_INF = -1,
};

/* Atari-ST trap emulator (see trap.s)
 */
static u8 trap_func[] = {
#include "sc68/trap68.h"
};

/* Atari-ST trap categories
 */
static const char * trap_type[16] =
{ 0, "gemdos", 0, 0, 0, 0 , 0, 0, 0, 0 , 0, 0, 0, "bios" , "xbios", 0 };

/** sc68 config */
static struct {
  int loaded;
  int allow_remote;
  int aga_blend;
#ifdef WITH_FORCE
  int force_track;
  int force_loop;
#endif
  int asid;
  int def_time_ms;
  int spr;
} config;

/** sc68 instance. */
struct _sc68_s {
  int            magic;       /**< magic identifier.                     */
  char           name[16];    /**< short name.                           */
  int            version;     /**< sc68 version.                         */
  void         * cookie;      /**< User private data.                    */
  emu68_parms_t  emu68_parms; /**< 68k emulator parameters.              */

/* Keep the following in that order !!!!
 * {
 */
  emu68_t      * emu68;       /**< 68k emulator instance.                */
  io68_t *ymio,*mwio,*shifterio,*paulaio,*mfpio;
/* } */

  ym_t         * ym;          /**< YM emulator.                          */
  mw_t         * mw;          /**< MicroWire emulator.                   */
  paula_t      * paula;       /**< Amiga emulator.                       */

  int            tobe3;       /**< free disk memory be release on close. */
  const disk68_t  * disk;     /**< Current loaded disk.                  */
  const music68_t * mus;      /**< Current playing music.                */
  int            track;       /**< Current playing track.                */
  int            track_to;    /**< Track to set (0:n/a, -1:end req).     */
  int            loop_to;     /**< Loop to set (0:default -1:infinite).  */
  int            asid;        /**< aSIDifier flags.                      */
  int            asid_timers; /**< timer assignment 4cc (0:not asid).    */
#ifdef WITH_FORCE
  int            cfg_track;   /**< from config "force-track".            */
  int            cfg_loop;    /**< from config "force-loop".             */
#endif
  int            cfg_asid;    /**< from config "asid".                   */

  unsigned int   playaddr;    /**< Current play address in 68 memory.    */
  int            seek_to;     /**< Seek to this time (-1:n/a)            */
  int            remote;      /**< Allow remote access.                  */

  struct {
    int org_ms;
    int len_ms;
  } tinfo[SC68_MAX_TRACK+1];

/** Playing time info. */
  struct {
    unsigned int def_ms;      /**< default time in ms.                   */
    unsigned int origin_ms;   /**< elapsed ms at track origin.           */
    unsigned int elapsed_ms;  /**< elapsed ms since track start.         */
  } time;

/** IRQ handler. */
  struct {
    int pc;                   /**< value of PC for last IRQ.             */
    int sr;                   /**< value of sr for lasr IRQ.             */
    int vector;               /**< what was the last IRQ type.           */
    int sysfct;               /**< Last gemdos/bios/xbios function       */
  } irq;

/** Mixer info struture. */
  struct
  {
    unsigned int   spr;          /**< Sampling rate in hz.               */
    u32          * buffer;       /**< Current PCM buffer.                */
    int            bufpos;       /**< Current PCM position.              */
    int            bufmax;       /**< buffer allocated size.             */
    int            bufreq;       /**< Required buffer size for track.    */
    int            buflen;       /**< PCM count in buffer.               */
    int            stdlen;       /**< Default number of PCM per pass.    */
    unsigned int   cycleperpass; /**< Number of 68K cycles per pass.     */
    int            aga_blend;    /**< Amiga LR blend factor [0..65535].  */

    unsigned int   pass_count;   /**< Pass counter.                      */
    unsigned int   loop_count;   /**< Loop counter.                      */
    unsigned int   pass_total;   /**< Total number of pass.              */
    unsigned int   loop_total;   /**< Total number of loop (0:infinite). */
    unsigned int   pass_2loop;   /**< Number of pass before next loop.   */
    unsigned int   pass_3loop;   /**< Reset pass_2loop after a loop.     */

  } mix;

  sc68_minfo_t     info;         /**< Disk and track info struct.        */

/* Error message */
  const char   * errstr;         /**< Last error message.                */
  char           errbuf[ERRMAX]; /**< For non-static error message.      */
};
