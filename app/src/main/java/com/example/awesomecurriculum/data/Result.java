package com.example.awesomecurriculum.data;

/**
 * 一个泛型类，它持有一个结果成功数据或一个错误异常。
 */
public class Result<T> {
    /** hide the private constructor to limit subclass types (Success, Error)*/
    private Result() {
    }

    /** 根据返回结果的成功与否生成一个字符串 */
    @Override
    public String toString() {
        if (this instanceof Result.Success) {
            Result.Success success = (Result.Success) this;
            return "Success[data=" + success.getData().toString() + "]";
        } else if (this instanceof Result.Error) {
            Result.Error error = (Result.Error) this;
            return "Error[exception=" + error.getError().toString() + "]";
        }
        return "";
    }

    /** 成功 子类 */
    public final static class Success<T> extends Result {
        private T data;

        public Success(T data) {
            this.data = data;
        }

        public T getData() {
            return this.data;
        }
    }

    /** 失败 子类*/
    public final static class Error extends Result {
        private Exception error;

        public Error(Exception error) {
            this.error = error;
        }

        public Exception getError() {
            return this.error;
        }
    }
}
