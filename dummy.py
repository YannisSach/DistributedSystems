from multiprocessing import Process,Queue
class Processor(Process):
    def __init__(self,queue):
        Process.__init__(self)
        self.que=queue
    def get_name(self):
        return "Process %s" % self.name
    def run(self):
        myque = Queue()
        self.que.put(myque)
        print (myque.get())



if __name__ == "__main__":

        processes = []
        for i in range(0,5):
                p=Processor(Queue())
                processes.append(p)
                p.start()
        for p in processes:
                q = p.que.get()
                q.put("HELLO")
                p.join()
                            
