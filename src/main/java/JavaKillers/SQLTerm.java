package JavaKillers;

public class SQLTerm {
    String _strTableName;
    String _strColumnName;
    String _strOperator;
    Comparable _objValue;
    public SQLTerm(String _strTableName,String _strColumnName,String _strOperator,Comparable _objValue ){
        this._strTableName=_strTableName;
        this._strColumnName=_strColumnName;
        this._strOperator=_strOperator;
        this._objValue=_objValue;
    }
    public SQLTerm(){

    }
}