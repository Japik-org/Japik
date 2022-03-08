package com.pro100kryto.server.utils;

import java.util.ArrayList;
import java.util.List;

public class ResolveDependenciesIncompleteException extends Throwable {
    private final Builder builder;

    private ResolveDependenciesIncompleteException(
            Builder builder) {
        this.builder = builder;
    }

    public List<Throwable> getWarningList() {
        return builder.warningList;
    }

    public List<Throwable> getErrorList() {
        return builder.errorList;
    }

    public boolean hasWarnings(){
        return builder.hasWarnings();
    }

    public boolean hasErrors(){
        return builder.hasErrors();
    }

    public boolean isEmpty(){
        return builder.isEmpty();
    }


    public static final class Builder{
        private final ArrayList<Throwable> warningList = new ArrayList<>();
        private final ArrayList<Throwable> errorList = new ArrayList<>();

        public void addWarning(Throwable throwable){
            warningList.add(throwable);
        }

        public boolean hasWarnings(){
            return !warningList.isEmpty();
        }

        public void addError(Throwable throwable){
            errorList.add(throwable);
        }

        public void addCause(ResolveDependenciesIncompleteException resolveDependenciesIncompleteException){
            errorList.addAll(resolveDependenciesIncompleteException.getErrorList());
            warningList.addAll(resolveDependenciesIncompleteException.getWarningList());
        }

        public boolean hasErrors(){
            return !errorList.isEmpty();
        }

        public boolean isEmpty(){
            return warningList.isEmpty() && errorList.isEmpty();
        }

        public ResolveDependenciesIncompleteException build(){
            return new ResolveDependenciesIncompleteException(this);
        }
    }
}
