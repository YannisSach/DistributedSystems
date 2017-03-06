import hashlib
import sys
import random
from multiprocessing import Process, Queue

m = 10
chord_size = pow(2,m)
SRC = 0
CMD = 1
KEY = 2
VAL = 3
CNT = 3 # ???

DEBUG = False

def debug (my_id,msg):
    if DEBUG:
        print(my_id + ":" + msg)

def identifier_fnc (key):
    key = str(key)
    key = key.encode('utf-8')
    sha = hashlib.sha1()
    sha.update(key)
    identifier = sha.hexdigest()
    identifier = int(identifier, 16)
    identifier = identifier % chord_size
    return identifier


class Node (Process):

    q_ins = {}
    nodes = 0

    def __init__(self, queue_pred, queue_in, queue_succ, node_num, low, high):
        super(Node, self).__init__()
        self.queue_pred = queue_pred
        self.queue_in = queue_in
        self.queue_succ = queue_succ
        self.node_num = node_num
        self.idx = identifier_fnc(node_num)
        self.low = low
        self.high = high
        self.bucket = {}
        # insert node chord TODO, join/departure always through node 1
        Node.nodes += 1
        Node.q_ins[str(idx)] = queue_in

    def is_mine (self, key):
        if self.low <= self.high:
            return self.low <= key <= self.high
        else:
            return self.low <= key or key <= self.high

    def run (self):

        print(self.q_ins)

        while (True):

            request = self.queue_in.get()
            request_lst = request.split(",")

            if request_lst[SRC] == self.idx:
                # we have a response and not a request
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


    def insert (self, key, val, id_src):
        # key: title of the song
        # value: the song
        debug(self.idx, "Trying to insert " + key)
        identifier = str(identifier_fnc(key))
        if self.is_mine(int(identifier)):
            debug(self.idx,key + " is mine!")
            if identifier in self.bucket:
                ls = self.bucket[identifier]
            else:
                ls = []
                self.bucket[identifier] = []
            for x in ls:
                if x[0] == key:
                    ls.remove(x)
            self.bucket[identifier].append((key,val))
            debug(self.idx, "Writing to queue " + id_src + " " + key)
            Node.q_ins[str(id_src)].put(id_src + ",INSERTED," + key + "," + val + "," + self.idx)
        else:
            # forward to the next node
            debug(self.idx,key + " not mine! Forwarding to next queue")
            self.queue_succ.put(id_src + ",INSERT," + key + "," + val)


    def query (self, key, id_src, cnt=0):
        # check if key is *
        if key != "*":
            identifier = identifier_fnc(key)
            if self.is_mine(identifier):
                ls = self.bucket[identifier]
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


    def delete (self, key, id_src):

        identifier = identifier_fnc(key)
        if self.is_mine(identifier):
            ls = self.bucket[identifier]
            for x in ls:
                if x[0] == key:
                    ls.remove(x)
                    Node.q_ins[id_src].put(id_src + ",DELETED," + key + "," + self.idx)
                    return
            Node.q_ins[id_src].put(id_src + ",DELETE_NOT_FOUND," + key + "," + self.idx)
        else:
            self.queue_succ.put(id_src + ",DELETE," + key)

def form (line):
    lst = line.split(",")
    return "init" + ",INSERT," + lst[0] + "," + lst[1]

if __name__ == "__main__":
    procs = []
    queues = [Queue() for _ in range(0,10)]
    for i in range(0,10):
        p = Node(queues[(i-1)%10], queues[i], queues[(i+1)%10],i,i,i)
        procs.append(p)
    # that will be a problem when nodes will join and depart
    for p in procs:
        p.start()
    for line in sys.stdin:
        pr = random.randint(0,2)
        queues[pr].put(form(line.rstrip("\n")))
