/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package untyped;

/**
 *
 * @author Marco
 */
public class Request {
    
    private int percentageDone;
    private double result;
    
    public Request(){
        percentageDone = 0;
    }
    
    public int getPercentageDone(){
        return percentageDone;
    }
        
    public double getResult(){
        return result;
    }
    
    public void setPercentageDone(int percentageDone){
        this.percentageDone = percentageDone;
    }
        
    public void setResult(double result){
        this.result = result;
    }
    
}
