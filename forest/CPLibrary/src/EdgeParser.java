import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Scanner;


public class EdgeParser {
	
	
	public static void main(String[] args) throws IOException{
		
		int maxnum=0;
		
		int targets=0;
		int sources=0;
		int edges=0;
		
		String output = "";
		
		File file = new File("edges.txt");
		
		FileReader reader = new FileReader(file);
		
		
		int next = reader.read();
		int n2;
		char r2;
		int n3;
		char r3;
		
		char r = (char) next;
		output+= r;
	
		while(next!=-1){
			
			next=reader.read();
			r = (char) next;
//
//			if(r=='{' || r=='}' || r==','){
//				output+= r;
//			}
			
			if(r=='A'){
				
			
			
			}else if(r=='C'){
				int num=0;
				
				next=reader.read();
				r = (char) next;
				
				n2=reader.read();
				r2 = (char) n2;
				
				if(r2!='}' && r2!=','){
					
					n3=reader.read();
					r3 = (char) n3;
					
					if(r3!='}'&&r3!=','){
						num+= Character.getNumericValue(r)*100;
						num+= Character.getNumericValue(r2)*10;
						num+= Character.getNumericValue(r3);
						next = reader.read();
						r = (char) next;
					}else{
						num+=Character.getNumericValue(r)*10;
						num+=Character.getNumericValue(r2);
						r=r3;
					}
				}else{
					num+= Character.getNumericValue(r);
					r= r2;
				}
				output+= 98+155+num;
				output+=r;

				if(98+122+num>maxnum){
					maxnum=98+122+num;
				}
				
			
			
			}else if(r=='B'){
				int num=0;
				
				next=reader.read();
				r = (char) next;
				
				n2=reader.read();
				r2 = (char) n2;
				
				if(r2!='}'&& r2!=','){
					
					n3=reader.read();
					r3 = (char) n3;
					
					if(r3!='}'&& r3!=','){
						num+= Character.getNumericValue(r)*100;
						num+= Character.getNumericValue(r2)*10;
						num+= Character.getNumericValue(r3);
						next = reader.read();
						r = (char) next;
					}else{
						num+=Character.getNumericValue(r)*10;
						num+=Character.getNumericValue(r2);
						r=r3;
					}
				}else{
					num+= Character.getNumericValue(r);
					r= r2;
				}
				output+= 98+num;
				output+=r;
				
			
			}else{
				output+= r;
			}
			
		}
		//System.out.println(maxnum);
		System.out.println(output);
		
//		for(int i=1;i<99;i++){
//			System.out.print(i+",");
//		}
		
//		System.out.println("\n");
//		System.out.println();
//		
//		for(int i=99;i<99+122;i++){
//			System.out.print(i+",");
//		}
		
		
	}
	
}
