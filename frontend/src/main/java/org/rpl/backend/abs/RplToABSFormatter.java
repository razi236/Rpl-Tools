package org.rpl.backend.abs;
public interface RplToABSFormatter {
    void beforeOpenBrace();
    void afterOpenBrace();
    void afterStmt();
    void beforeCloseBrace();
    void afterCloseBrace();
    
}
