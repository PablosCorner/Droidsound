/******************************************************************************\
* Authors:  Iconoclast                                                         *
* Release:  2013.11.26                                                         *
* License:  CC0 Public Domain Dedication                                       *
*                                                                              *
* To the extent possible under law, the author(s) have dedicated all copyright *
* and related and neighboring rights to this software to the public domain     *
* worldwide. This software is distributed without any warranty.                *
*                                                                              *
* You should have received a copy of the CC0 Public Domain Dedication along    *
* with this software.                                                          *
* If not, see <http://creativecommons.org/publicdomain/zero/1.0/>.             *
\******************************************************************************/
#include "vu.h"

INLINE static void do_madn(usf_state_t * state, short* VD, short* VS, short* VT)
{

#ifdef DISABLED_ARCH_MIN_ARM_NEON

	int16x8_t vs,vt, vaccm, vacch, vacc_h,vaccl,one,cond,zero,minus1;
		 	     
	zero = vdupq_n_s16(0);
	minus1 = vdupq_n_s16(-1);
    vs = vld1q_s16((const int16_t *)VS);
    vt = vld1q_s16((const int16_t *)VT);
	vaccl = vld1q_s16((const int16_t *)VACC_L);
	vaccm = vld1q_s16((const int16_t *)VACC_M);
	vacch = vld1q_s16((const int16_t *)VACC_H);
	
	int16x4_t low_a = vget_low_s16(vs);
	int16x4_t low_b = vget_low_s16(vt);
	int16x4_t high_a = vget_high_s16(vs);
	int16x4_t high_b = vget_high_s16(vt);
	uint32x4_t low = vmull_u16((uint16x4_t)low_a, (uint16x4_t)low_b);
	int32x4_t high = vmull_s16(high_a, high_b);

	int16x8x2_t res = vuzpq_s16((int16x8_t)low,(int16x8_t)high);
		
	int16x8_t vacc_l = vaddq_s16(res.val[0],(int16x8_t)vaccl);
	int16x8_t vacc_m = vaddq_s16(res.val[1],(int16x8_t)vaccm);

	cond = vminq_s16(vaccl, zero);
	cond = vmaxq_s16(cond, minus1);
	cond = vnegq_s16(cond);
	
	int32x4_t vacc_1 = vmovl_s16(vget_low_s16(res.val[1]));
	int32x4_t vacc_2 = vmovl_s16(vget_high_s16(res.val[1]));
	uint32x4_t vacc_h1 = vaddq_u32((uint32x4_t)vacc_1,vmovl_u16(vget_low_u16((uint16x8_t)vaccm)));
	uint32x4_t vacc_h2 = vaddq_u32((uint32x4_t)vacc_2,vmovl_u16(vget_high_u16((uint16x8_t)vaccm)));

	int16x8x2_t vacc_htemp = vuzpq_s16((int16x8_t)vacc_h1,(int16x8_t)vacc_h2);
	
	vacc_m = vaddq_s16(vacc_m, cond);
	vacc_h = vaddq_s16(vacch, vacc_htemp.val[1]);
	
	vst1q_s16(VACC_L, vacc_l);
	vst1q_s16(VACC_M, vacc_m);
	vst1q_s16(VACC_H, vacc_h);
	SIGNED_CLAMP_AL(state, VD);
	return;
		
#endif

    ALIGNED uint32_t addend[N];
    register int i;

    for (i = 0; i < N; i++)
        addend[i] = (unsigned short)(VACC_L[i]) + (unsigned short)(VS[i]*VT[i]);
    for (i = 0; i < N; i++)
        VACC_L[i] += (short)(VS[i] * VT[i]);
    for (i = 0; i < N; i++)
        addend[i] = (addend[i] >> 16) + ((unsigned short)(VS[i])*VT[i] >> 16);
    for (i = 0; i < N; i++)
        addend[i] = (unsigned short)(VACC_M[i]) + addend[i];
    for (i = 0; i < N; i++)
        VACC_M[i] = (short)addend[i];
    for (i = 0; i < N; i++)
        VACC_H[i] += addend[i] >> 16;
    SIGNED_CLAMP_AL(state, VD);
    return;
}

static void VMADN(usf_state_t * state, int vd, int vs, int vt, int e)
{
    ALIGNED short ST[N];

    SHUFFLE_VECTOR(ST, state->VR[vt], e);
    do_madn(state, state->VR[vd], state->VR[vs], ST);
    return;
}
