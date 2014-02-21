#!/usr/bin/python
import sqlite3
import sys
import os

def main(argv) :

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
    seen_paths0 = set()
    seen_paths1 = set()
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

        rootpath = "/mnt/sdcard/dsroot"

        if len(parts) == 3:
            path, filename = os.path.split(name)
            path1, path2 = os.path.split(path)
            format = os.path.splitext(filename)[-1][1:].upper()

        elif len(parts) == 4:
            game = parts[2]
            path, filename = os.path.split(name)
            path1, path2 = os.path.split(path)
            format = os.path.splitext(name)[-1][1:].upper()

        else:
            print "Strange line ", parts
            raise Exception('Unknown format')

        try:
            if (rootpath+path1) not in seen_paths0:
                dbc.execute('insert into FILES values (null, ?,?,?,?,?,?)', (rootpath, '', 512, path1, '', ''))
                seen_paths0.add(rootpath+path1)

            if (rootpath+path1+path2) not in seen_paths1:
                dbc.execute('insert into FILES values (null, ?,?,?,?,?,?)', (rootpath+"/"+path1, '', 512, path2, '', ''))
                seen_paths1.add(rootpath+path1+path2)

            seen_paths.add(path)

            dbc.execute('insert into FILES values (null, ?,?,?,?,?,?)', ("/mnt/sdcard/dsroot/"+path, filename, 1024, filename, author, ''))

        except :
            print "Could not insert ",name, type, game, author
            raise

    db.commit()
    db.close()



if __name__ == "__main__":
	main(sys.argv[1:])
