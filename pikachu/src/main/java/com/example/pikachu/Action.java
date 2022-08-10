package com.example.pikachu;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;

public class Action<T> {
    static class RequestWeakReference<M> extends WeakReference<M> {
        final Action action;

        public RequestWeakReference(Action action, M referent, ReferenceQueue<? super M> q) {
            super(referent, q);
            this.action = action;
        }
    }

    final Pikachu pikachu;
    final Request request;
    final WeakReference<T> target;
    final String stringKey;

    Action(Pikachu pikachu, Request request, T target, String stringKey) {
        this.pikachu = pikachu;
        this.request = request;
        this.target =
                target == null ? null : new RequestWeakReference<T>(this, target, pikachu.referenceQueue);
        this.stringKey = stringKey;
    }

    T getTarget() {
        return target == null ? null : target.get();
    }

    String getKey() {
        return stringKey;
    }

    Request getRequest() {
        return request;
    }

    Pikachu getPicasso() {
        return pikachu;
    }
}
