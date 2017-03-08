from multiprocessing import Process
import socket

class Processor(Process):
    def __init__(self):
        Process.__init__(self)
    def get_name(self):
        return "Process %s" % self.name
    def run(self):
        s = socket.socket( socket.AF_INET, socket.SOCK_STREAM)
        s.connect(("localhost",5000))
        print("Done!")
        s.send(b"HELLO!")


class Processor2(Process):
    def __init__(self):
        Process.__init__(self)
    def get_name(self):
        return "Process %s" % self.name
    def run(self):
        server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server.bind(('localhost',5000))
        server.listen(10)
        (c,a) = server.accept()
        print(c)
        msg = c.recv(len("HELLO"))
        print(msg)
        



if __name__ == "__main__":

        processes = []
        server = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        server.bind(('localhost',5000))
        server.listen(10)
        
        for i in range(0,1):
                p=Processor()
                processes.append(p)
                p.start()

        (c,a) = server.accept()
        print(c)
        msg = c.recv(len("HELLO"))
        print(msg)

       
      
