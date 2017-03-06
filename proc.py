import hashlib
import sys
import random
from multiprocessing import Process, Queue



chord_size = 3
SRC = 0
CMD = 1
KEY = 2
VAL = 3
CNT = 3

DEBUG = False

def debug(my_id,msg):
    if DEBUG: print(my_id+":"+msg)

def hashf (key):
    sha = hashlib.sha1()
    key = key.encode('utf-8')
    sha.update(key)
    hashed_key = sha.hexdigest()
    hashed_key = int(hashed_key, 16)
    return (hashed_key)% chord_size


class Node(Process):
    q_ins = {}
    nodes = 0
    
    def __init__(self,queue_pred, queue_in, queue_succ, idx, low, high):
        super(Node, self).__init__()
        self.queue_pred = queue_pred
        self.queue_in = queue_in
        self.queue_succ = queue_succ
        self.idx = str(idx)
        self.low = low
        self.high = high
        self.bucket = {}
        # insert node chord TODO
        Node.nodes += 1 
        Node.q_ins[str(idx)] = queue_in

    def is_mine (self,key):
        if self.low <= self.high:
            return self.low <= key <= self.high
        else:
            return self.low <= key or key <= self.high

    def run(self):
        print(self.q_ins)
        while(True):
            request = self.queue_in.get()
            request_lst = request.split(",")

            if request_lst[SRC] == self.idx:
            #we have a response and not a request
                print(self.idx + ": " + request)
            else:
                if request_lst[SRC] == "init":
                    request_lst[SRC] = self.idx
                if request_lst[CMD] == "INSERT":
                    self.insert(request_lst[KEY],request_lst[VAL],request_lst[SRC])
                elif request_lst[CMD] == "QUERY":
                    if request_lst[KEY] == "*":
                        if int(request_lst[CNT]) < Nodes.nodes:
                            self.query(request_lst[KEY],request_lst[SRC],request_lst[CNT])
                    else:
                        self.query(request_lst[KEY],request_lst[SRC])
                elif request_lst[CMD] == "DELETE":
                    self.delete(request_lst[KEY],request_lst[SRC])


    def insert (self,key, val, id_src):
        # key: title of the song
        # value: the song
        debug(self.idx,"Trying to insert " + key)
        hashed_key = str(hashf(key))
        if self.is_mine(int(hashed_key)):
            debug(self.idx,key + " is mine!")
            if hashed_key in self.bucket:
                ls = self.bucket[hashed_key]
            else:
                ls = []
                self.bucket[hashed_key] = []
            for x in ls:
                if x[0] == key:
                    ls.remove(x)
            self.bucket[hashed_key].append((key,val))
            debug(self.idx, "Writing to queue " + id_src + " " + key)
            Node.q_ins[str(id_src)].put(id_src + ",INSERTED," + key + "," + val + "," + self.idx)
        else:
            # forward to the next node
            debug(self.idx,key + " not mine! Forwarding to next queue")
            self.queue_succ.put(id_src + ",INSERT," + key + "," + val)
                    

    def query (self,key,id_src,cnt=0):
        # check if key is *
        if key != "*":
            hashed_key = hashf(key)
            if self.is_mine(hashed_key):
                ls = self.bucket[hashed_key]
                for x in ls:
                    if x[0] == key:
                        Node.q_ins[id_src].put(id_src + ",FOUND," + key + "," + val + "," + self.idx) 
                        return
                Node.q_ins[id_src].put(id_src + ",QUERY_NOT_FOUND," + key + "," + self.idx) 
                return
            else:
                self.queue_succ.put(id_src + ",QUERY," + key)
        else:
            # the first node that finds the * will start with cnt = 0 and increment
            result = id_src + ",STAR_FOUND,"
            for x in ls:
                Node.q_ins[id_src].put(result + x[0] + "," + x[1] + "," + self.idx)
            cnt += 1 
            self.queue_succ.put(id_src + ",QUERY," + key + "," + cnt)
        

    def delete (self,key,id_src):

        hashed_key = hashf(key)
        if self.is_mine(hashed_key):
            ls = self.bucket[hashed_key]
            for x in ls:
                if x[0] == key:
                    ls.remove(x)
                    Node.q_ins[id_src].put(id_src + ",DELETED," + key + "," + self.idx)
                    return
            Node.q_ins[id_src].put(id_src + ",DELETE_NOT_FOUND," + key + "," + self.idx)
        else:
            self.queue_succ.put(id_src + ",DELETE," + key)

def form(line) :
    lst = line.split(",")
    return "init" + ",INSERT," + lst[0] + "," + lst[1]  

if __name__ == "__main__":
    procs = []
    queues = [Queue() for _ in range(0,3)]
    for i in range(0,3):
        p = Node(queues[(i-1)%3], queues[i], queues[(i+1)%3],i,i,i)
        procs.append(p)

    for p in procs:
        p.start()
    for line in sys.stdin:
        pr = random.randint(0,2)
        queues[pr].put(form(line.rstrip("\n")))
        
    
