package com.ty.tybusbase.utils

import android.content.Context
import com.trello.rxlifecycle4.LifecycleProvider
import com.trello.rxlifecycle4.LifecycleTransformer
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.ObservableTransformer
import io.reactivex.rxjava3.schedulers.Schedulers

object RxUtils {
    /**
     * 生命周期绑定
     *
     * @param lifecycle Activity
     * @return
     */
    fun <T> bindToLifecycle(lifecycle: Context): LifecycleTransformer<T> {
        return if (lifecycle is LifecycleProvider<*>) {
            (lifecycle as LifecycleProvider<*>).bindToLifecycle()
        } else {
            throw IllegalArgumentException("context not the LifecycleProvider type")
        }
    }

    /**
     * 生命周期绑定
     *
     * @param lifecycle Fragment
     * @return
     */
    fun <T> bindToLifecycle(lifecycle: LifecycleProvider<*>): LifecycleTransformer<T> {
        return lifecycle.bindToLifecycle()
    }

    /**
     * 线程调度器
     * @return
     */
    fun <T : Any> schedulersTransformer(): ObservableTransformer<T, T> {
        return ObservableTransformer { observable ->
            observable.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
        }
    } //    /**
    //     * 网络异常转换器
    //     * @return
    //     */
    //    public static <T>ObservableTransformer<T,T> exceptionTransformer(){
    //        return new ObservableTransformer<T,T>() {
    //            @NotNull
    //            @Override
    //            public ObservableSource<T> apply(@NotNull Observable observable) {
    //                return observable.onErrorResumeNext(new HttpResponseFunc());
    //            }
    //        };
    //    }
    //    /**
    //     * 异常调度
    //     * @param <T>
    //     */
    //    private static class HttpResponseFunc<T> implements Function<Throwable,Observable<T>> {
    //        @Override
    //        public Observable<T> apply(@NotNull Throwable throwable) {
    //            return Observable.error(ExceptionHandle.handleException(throwable));
    //        }
    //    }
}