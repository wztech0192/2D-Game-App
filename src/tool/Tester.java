package tool;

public class Tester {
	public static boolean isNumber(String s) {	
 		if(s.isEmpty()) {
			return false;
		}
        int start=0;
        boolean dec=false;
        boolean e=false;
        boolean plus=false;
        boolean minus=false;
        int end=s.length()-1;   
        char[] c=s.toCharArray();  
        while(c[start]==' '){
            start++;
            if(start==s.length()) {
            	return false;
            }
        }
        while(c[end]==' '){
            end--;
        }
        if(c[start]=='.'){
            if(start==end){
                return false;
            }
           else if(!Character.isDigit(c[start+1])){
                		return false;
                	}
            else dec=true;
        }
        else if(c[start]=='-'||c[start]=='+'){
            if(start==end){
                return false;
            }
        }
        else if(!Character.isDigit(c[start])){
            return false;
        }
        while(start<end){
            start++; 
            if(c[start]=='e'){
                if(e || (start)==end){
                    return false;
                }                
                else if(!Character.isDigit(c[start-1]) && c[start-1]!='.') { 
                	return false;
                }else e=true;
            }
            else if(c[start]=='.'){
                if(dec||e){
                    return false;
                }
                else if(!Character.isDigit(c[start-1])) {
                
                	if(start==end) {
                		return false;
                	}
                	else if(!Character.isDigit(c[start+1]) ){
     
                		return false;
                	}
                }
                else dec=true;
            }
            else if(c[start]=='+'){
                if(!e || plus || start==end){
                    return false;
                }
                else if(!Character.isDigit(c[start-1])) {

                	if(!Character.isDigit(c[start+1]) ){
     
                		return false;
                	}
                }
                else plus=true;

            }
            else if(c[start]=='-'){
                if(!e || minus || start==end){
                    return false;
                }
                else if(!Character.isDigit(c[start-1])) {

                	if(!Character.isDigit(c[start+1]) ){
     
                		return false;
                	}
                }
                else minus=true;
                
            }   
            else if(!Character.isDigit(c[start])){
                return false;
            } 
        }   
        return true;   
    }
}
