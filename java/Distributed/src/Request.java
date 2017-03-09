
public class Request {
	public int src,cnt,id,port;
	public String cmd,key,val;
	
	
	public static int SRC=0,CMD=1,KEY=2,VAL=3,CNT=3,ID=2,PORT=3;
	
	public Request(String request){
		this.request = request;
		String[] requestLst = request.split(",");
		
	}
	
	
}
