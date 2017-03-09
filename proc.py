import hashlib
import sys
import random
from multiprocessing import Process, Queue
import queue as qq
import socket
import time
import threading

lock = threading.RLock()
# m = 10 chord = pow(2,10)
default_port = 24900
chord_size = 1000
next_port = default_port
SRC = 0
CMD = 1
KEY = 2
VAL = 3
CNT = 3
ID = 2
PORT = 3
RSIZE = 15

DEBUG = True

def busy_accept(s):
    while(True):
        try:
            (conn,a) = s.accept()
            return (conn,a)
        except socket.error:
            time.sleep(1)
            continue

def debug(my_id,msg):
    if DEBUG: print(my_id+":"+msg)

def hashf (key):
    sha = hashlib.sha1()
    key = str(key).encode('utf-8')
    sha.update(key)
    hashed_key = sha.hexdigest()
    hashed_key = int(hashed_key, 16)
    return (hashed_key)% chord_size

def get_next_port():
    global next_port
    next_port += 1
    temp = next_port
    return temp

def build_socket_server(port):
    my_ss = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    my_ss.bind(('localhost',port))
    my_ss.listen(10)
    my_ss.setblocking(0)
    return my_ss

def build_socket_client(port):
    debug("?" ,  "trying to build socket")
    my_sc = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    my_sc.connect(("localhost",port))
    return my_sc


def is_between (key,low,high):
    if int(low) <= int(high):
        return int(low) <= int(key) <= int(high)
    else:
        return int(low) <= int(key) or int(key <= high)

class PendingRequest(Process):

    def __init__(self, port):
        super(PendingRequest, self).__init__()
        while True:
            try:
                self.port = get_next_port()
                self.my_socket = build_socket_server(self.port)
                debug(str(self.port),"PendingRequest is waiting on port: " + str(self.port))
                break
            except socket.error:
                continue

    def run(self):
        debug(str(self.port), "Waiting to accept")
        (self.c,a) = busy_accept(self.my_socket)
        debug(str(self.port),"Connection established with pending request at address: " + str(a))
        print(str(self.c.recv(RSIZE)))


class Server(Process):
    q_ins = {}
    nodes = 0

    def __init__(self,my_q, idx, my_id):
        super(Server, self).__init__()
        #self.port_pred = port_pred
        #self.port_succ = port_succ 
        self.my_q = my_q
        self.idx = idx
        self.my_id = str(my_id)
        self.bucket = {}
        while True:
            try:
                self.my_port = get_next_port()
                debug(str(idx), "My port is " + str(self.my_port))
                self.my_ss = build_socket_server(self.my_port)
                break
            except socket.error:
                debug(str(idx), "Port is already in use trying next...")
                continue
        # insert node chord TODO
        Server.nodes += 1
        if idx == 1:
            self.next_id = str(my_id)
            self.prev_id = str((my_id+1)%chord_size)
            self.next_socket = None
            self.prev_socket = None
        #Server.q_ins[str(idx)] = queue_in

    def wait_for_request(self):
        while True:
            try :
                r = self.my_q.get_nowait()
                return r
            except qq.Empty:
                if Server.nodes > 1:
                    try:
                        r = self.prev_socket.recv(10)
                        return r.decode()
                    except socket.error:
                        continue
                else:
                    continue

            #SLEEP...

    def join_request(self,request,rq_lst):
        debug(str(self.idx), "Entered join request")
        if self.next_socket == None or self.prev_socket == None:
            new_prev_sc = build_socket_client(rq_lst[PORT])
            self.prev_socket = new_prev_sc
            self.prev_id = rq_lst[ID]
            debug(str(self.idx), "Connected with the pred at port:" + str(rq_lst[PORT]))
            new_next_sc = build_socket_client(rq_lst[PORT])
            debug(str(self.idx), "Connected with the succ at port:" + str(rq_lst[PORT]))
            self.next_socket = new_next_sc
            self.next_id = rq_lst[ID]
            return

        if is_between(rq_lst[ID],self.prev_id, self.my_id):
            new_prev_sc = build_socket_client(rq_lst[PORT])
            debug(str(self.idx), "Connected with the pred")
            self.prev_socket.close()
            self.prev_socket = new_prev_sc
            self.prev_id = rq_lst[ID]
            self.prev_socket.send(self.my_id.encode())
                  #TODO transfer
            return

        if is_between(rq_lst[ID],self.my_id,self.next_id):
            new_next_sc = build_socket_client(rq_lst[PORT])
            debug(str(self.idx), "Connected with the succ")
            self.next_socket.send(request.encode)
            self.next_socket.close()
            self.next_socket = new_next_sc
            self.next_id = rq_lst[ID]
            self.next_socket.send(self.my_id.encode())
            return

    def run(self):

        if Server.nodes > 1: #not the first Node
            debug(str(self.idx),"Trying to connect")
            (pred_conn,a) = busy_accept(self.my_ss)
            debug(str(self.idx), "Pred connection established with address:" + str(a))
            (succ_conn,a) = busy_accept(self.my_ss)
            debug(str(self.idx), "Succ connection established with address:" + str(a))

            while(True):
                prv_id = pred_conn.recv(RSIZE)
                if prv_id:
                  self.prev_id = prv_id.decode()
                  debug(str(self.idx), "Received previous id:" + str(self.prev_id))
                  break
            while(True):
                nxt_id = succ_conn.recv(RSIZE)
                if nxt_id:
                  self.next_id = nxt_id.decode()
                  debug(str(self.idx), "Received next id:" + str(self.next_id))
                  break
        #print(self.q_ins)?
        while(True):
            debug(str(self.idx),"Hashed left id " + self.prev_id)
            debug(str(self.idx),"Hashed right id " + self.next_id)
            debug(str(self.idx),"Hashed myId " + self.my_id)

            debug(str(self.idx),"Waiting for req...")
            request = self.wait_for_request()
            debug(str(self.idx),"Got req: " + request)
            request_lst = request.split(",")
            if request_lst[SRC] == "init":
                port = get_next_port()
                pr = PendingRequest(port)
                pr.start()
                debug(str(self.idx), request_lst[SRC])
                request_lst[SRC] = pr.port
                debug(str(self.idx), "When resolved this request will be sent to port:" + str(pr.port))
            if request_lst[CMD] == "JOIN":
                debug(str(self.idx), "Starting join_request protocol")
                self.join_request(request,request_lst)
                debug(str(self.idx), "Join was successful")
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
    queues = [Queue() for _ in range(0,10)]
    #_(self,my_q, idx, my_id, low, high):
    f = Server(queues[1],1,hashf(1))
    f.start()
    for i in range(2,10):
        p =Server(queues[i],i,hashf(i))
        p.start()
        procs.append(p)
        debug("Coord", "Adding new request...")
        queues[1].put("init,JOIN," + str(hashf(i)) + "," + str(p.my_port) + "," + str(i))
        time.sleep(1)
