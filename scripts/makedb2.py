#!/usr/bin/python
import sqlite3
import sys
import os
from ftplib import FTP

SERVER = "ftp.modland.com"

KNOWN_FORMATS = ["PSID", "RSID", "SID", "PRG",
                "PSF", "PSF2", "MINIPSF", "MINIPSF2",
                "DSF", "MINIDSF", "SSF", "MINISSF",
                "GSF", "MINIGSF",
                "QSF", "MINIQSF",
                "2SF", "MINI2SF",
                "SGC", "SFM", "SPC", "GYM", "NSF", "NSFE", "GBS", "AY", "SAP", "HES", "KSS", "VGM", "VGZ",
                "AHX", "HVL",
                "MP3",
                "MOD", "S3M", "XM", "IT", "MPTM", "STM", "NST", "M15", "STK", "WOW", "ULT", "669", "MTM", "MED", "FAR", "MDL", "AMS", "DSM", "AMF", "OKT", "DMF", "PTM", "PSM", "MT2", "DBM", "DIGI", "IMF", "J2B", "GDM", "UMX",
                "RSN", "RMU",
                "SNDH", "SC68", "SND",

                "CUS","CUST","CUSTOM","SMOD","TFX","SNG","RJP","JPN",
                "AST","AHX","THX","ADPCM","AMC","ABK","AAM","ALP","AON","AON4","AON8","ADSC","MOD_ADSC4",
                "BSS","BD","BDS","UDS","KRIS","CIN","CORE","CM","RK","RKB","DZ","MKIIO","DL","DL_DELI",
                "DLN","DH","DW","DWOLD","DLM2","DM","DLM1","DM1","DSR","DB","DIGI","DSC","DSS","DNS",
                "EMS","EMSV6","EX","FC13","FC3","FC14","FC4","FC","FRED","GRAY","BFC","BSI","FC-BSI",
                "FP","FW","GLUE","GM","EA","MG","HD","HIPC","EMOD","QC","IMS","DUM","IS","IS20","JAM",
                "JC","JMF","JCB","JCBO","JPN","JPND","JP","JT","MON_OLD","JO","HIP","HIP7","S7G","HST",
                "SOG","SOC","KH","POWT","PT","LME","MON","MFP","HN","MTP2","THN","MC","MCR","MCO","MK2",
                "MKII","AVP","MW","MAX","MCMD","MED","MMD0","MMD1","MMD2","MSO","MIDI","MD","MMDC","DMU",
                "MUG","DMU2","MUG2","MA","MM4","MM8","MMS","NTP","TWO","OCTAMED","OKT","ONE","DAT","PS",
                "SNK","PVP","PAP","PSA","MOD_DOC","MOD15","MOD15_MST","MOD_NTK","MOD_NTK1","MOD_NTK2",
                "MOD_NTKAMP","MOD_FLT4","MOD","MOD_COMP","!PM!","40A","40B","41A","50A","60A","61A",
                "AC1","AC1D","AVAL","CHAN","CP","CPLX","CRB","DI","EU","FC-M","FCM","FT","FUZ","FUZZ",
                "GMC","GV","HMC","HRT","HRT!","ICE","IT1","KEF","KEF7","KRS","KSM","LAX","MEXXMP","MPRO",
                "NP","NP1","NP2","NOISEPACKER2","NP3","NOISEPACKER3","NR","NRU","NTPK","P10","P21","P30",
                "P40A","P40B","P41A","P4X","P50A","P5A","P5X","P60","P60A","P61","P61A","P6X","PHA","PIN",
                "PM","PM0","PM01","PM1","PM10C","PM18A","PM2","PM20","PM4","PM40","PMZ","POLK","PP10",
                "PP20","PP21","PP30","PPK","PR1","PR2","PROM","PRU","PRU1","PRU2","PRUN","PRUN1","PRUN2",
                "PWR","PYG","PYGM","PYGMY","SKT","SKYT","SNT","SNT!","ST2","ST26","ST30","STAR","STPK",
                "TP","TP1","TP2","TP3","UN2","UNIC","UNIC2","WN","XAN","XANN","ZEN","PUMA","RJP","SNG",
                "RIFF","RH","RHO","SA-P","SCUMM","S-C","SCN","SCR","SID1","SMN","SID2","MOK","SA","SONIC",
                "SA_OLD","SMUS","SNX","TINY","SPL","SC","SCT","PSF","SFX","SFX13","TW","SM","SM1","SM2",
                "SM3","SMPRO","BP","SNDMON","BP3","SJS","JD","DODA","SAS","SS","SB","JPO","JPOLD","SUN",
                "SYN","SDR","OSP","ST","SYNMOD","TFMX1.5","TFHD1.5","TFMX7V","TFHD7V","TFMXPRO","TFHDPRO",
                "TFMX","MDST","MDAT","THM","TF","TME","SG","DP","TRC","TRO","TRONIC","MOD15_UST","VSS",
                "WB","YM","ML","MOD15_ST-IV","AGI","TPU","QPA","SQT","QTS",

                "AAAP", "AAX", "ADX", "ADP", "AFC", "AGSC", "AHX", "AIX", "AMTS", "ASS", "ASF", "ASR",
    			"AST",  "AUS", "BAF", "BG00", "BMDX", "BGW", "BNS", "BRSTM", "CAF", "CAPDSP", "CCC",
    			"CFN", "CNK", "BCWAV", "DE2", "DSP", "DXH", "EAM", "ENTH", "EXST", "FAG", "FILP", "FSB",
    			"GCA", "GCM", "GCSW", "GCW", "GENH", "GMS", "GSB", "HGC1", "HPS", "IDSP", "IKM",
    			"ILD", "INT", "ISH", "IVB", "JOE", "KCES", "KCEY", "KHV", "LEG", "LOGG", "MATX", "MCG",
    			"MIB", "MIHB", "MIC", "MPDSP", "MSA", "MSS", "MSVP", "MUSC", "MUSX", "NPSF", "PDT",
    			"PNB", "PSH", "PSW", "RIFF", "RKV", "RND", "RRDS", "RSD", "RSF", "RSTM", "RWS", "RXW",
    			"SAD", "SCD", "SEG", "SFS",	"SL3", "SPD", "SPM", "SS2", "STR", "STS", "STRM", "STER", "STX",
    			"STS", "SVAG", "SVS", "SWAV", "TEC", "THP", "VAS", "VAG", "VIG", "VOI", "VPK", "VSF", "WAA",
    			"WAM", "WAVM", "WP2", "WSI", "WVS", "XMU", "XA", "XA2", "XSS", "XWB", "WVAS", "WWAV",
    			"YMF"
                ]

def get_modarch():
    ftp = FTP(SERVER)
    ftp.login()
    ftp.retrbinary('RETR allmods.zip', open('allmods.zip', 'wb').write)
    ftp.quit()
    return

def main(argv) :
    if os.path.isfile("modland.moddb"):
        os.remove('modland.moddb')

    db = sqlite3.connect('modland.moddb')
    db.text_factory = str

    dbc = db.cursor()
    try :
    	dbc.execute('create table FILES (_id INTEGER PRIMARY KEY, PATH TEXT, FILENAME TEXT, TYPE INTEGER, TITLE TEXT, COMPOSER TEXT, DATE INTEGER)')
    	dbc.execute('create index nameidx on FILES (PATH)')
    	db.commit()
    except :
    	print "DB FAILED"
    	return 0

    seen_paths = set()
    for l in open('allmods.txt'):
        name = l.split('\t')[1].strip()
        parts = name.split('/')
        if parts[0] == 'Ad Lib' or parts[0] == 'Video Game Music':
        	parts = [parts[0] + '/' + parts[1]] + parts[2:]
        if parts[0] == 'YM' and parts[1] == 'Synth Pack':
        	parts = [parts[0] + '/' + parts[1]] + parts[2:]

        if parts[2].startswith('coop-') :
        	parts = [parts[0]] + [parts[1] + '/' + parts[2]] + parts[3:]

        if len(parts) == 5 and (parts[3].startswith('instr') or parts[3].startswith('songs')) :
        	parts = parts[:2] + [parts[3] + '/' + parts[4]]

        if len(parts) > 4 :
        	parts = parts[:2] + ['/'.join(parts[3:])]

        type = parts[0]
        author = parts[1]
        game = ''
        date = -1

        _rootpath = "/mnt/sdcard/dsroot/MlDB"
        rootpath = _rootpath

        if len(parts) == 3:
            path, filename = os.path.split(name)
            folders = path.split("/")
            format = os.path.splitext(filename)[-1][1:].upper()

        elif len(parts) == 4:
            game = parts[2]
            path, filename = os.path.split(name)
            folders = path.split("/")
            format = os.path.splitext(name)[-1][1:].upper()

        else:
            print "Strange line ", parts
            raise Exception('Unknown format')

        if format not in KNOWN_FORMATS:
            continue

        try:
            for folder in folders:
                if (rootpath+"/"+folder) not in seen_paths:
                    dbc.execute('insert into FILES values (null, ?,?,?,?,?,?)', (rootpath, folder, 512, folder, '', ''))
                    rootpath += "/"+folder
                    seen_paths.add(rootpath)
                else:
                    rootpath += "/"+folder

            dbc.execute('insert into FILES values (null, ?,?,?,?,?,?)', (rootpath, filename, 1024, filename, author, ''))

        except :
            print "Could not insert ",name, type, game, author
            raise

    db.commit()
    db.close()



if __name__ == "__main__":
	main(sys.argv[1:])
