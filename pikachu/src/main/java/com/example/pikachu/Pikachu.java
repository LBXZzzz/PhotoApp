package com.example.pikachu;

import android.content.Context;

import java.util.concurrent.ExecutorService;

public class Pikachu {

    static volatile Pikachu singPikachu = null;

    private Context context;
    private ExecutorService service;
    private Cache cache;
    private Listener listener;
    private RequestTransformer transformer;
    public Pikachu(Context context,ExecutorService service,Cache cache,Listener listener,RequestTransformer transformer){
        this.context=context;
        this.service=service;
        this.cache=cache;
        this.listener=listener;
        this.transformer=transformer;
    }

    //调用这个方法获取到Pikachu实例
    public void with(Context context){

    }


    public static class Builder{

        private Context context;
        private ExecutorService service;
        private Cache cache;
        private Listener listener;
        private RequestTransformer transformer;

        public Builder(Context context) {
            if (context == null) {
                throw new IllegalArgumentException("Context must not be null.");
            }
            this.context = context.getApplicationContext();
        }

       /* public Pikachu build(){
            Context context=this.context;
            if (cache == null) {
                cache = new LruCache(context);
            }
            if (service == null) {
                service = new PicassoExecutorService();
            }

//            return new Pikachu(context,cache,listener,transformer);

        }*/
    }
}
