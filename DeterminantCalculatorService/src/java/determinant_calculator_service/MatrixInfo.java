package determinant_calculator_service;

public class MatrixInfo {
    
    private double[][] matrix;
    private int matrixLength;
	private int nRowsDone;
    private double determinant;
	private boolean changeSign;
    
    public MatrixInfo(double[][] matrix){
        this.matrix = matrix;
        matrixLength = matrix.length;
        nRowsDone = 0;
        determinant = 1;
        changeSign = false;
    }
    
    public double[][] getMatrix(){
        return matrix;
    }
        
    public int getMatrixLength(){
        return matrixLength;
    }
        
    public int getRowsDone(){
        return nRowsDone;
    }
        
    public double getDeterminant(){
        return determinant;
    }
        
    public boolean getChangeSign(){
        return changeSign;
    }
    
    public void setMatrix(double[][] matrix){
        this.matrix = matrix;
    }    
    
    public void setDeterminant(double determinant){
        this.determinant = determinant;
    }   
    
    public void setRowsDone(int nRowsDone){
        this.nRowsDone = nRowsDone;
    }
    
    public void setChangeSign(){
        changeSign = !changeSign;
    }
    
}
