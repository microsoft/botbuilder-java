package com.microsoft.bot.connector.authentication;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

public class ResponseFuture implements Callback {
    public final CompletableFuture<Response> future = new CompletableFuture<Response>();
    public Call call;

    public ResponseFuture(Call call) {
        this.call = call;
    }

    @Override public void onFailure(Call call, IOException e) {
        future.completeExceptionally(e);
    }

    @Override public void onResponse(Call call, Response response) throws IOException {
        future.complete(response);
    }
}