package com.ilife.iliferobot;

import androidx.test.filters.LargeTest;
import androidx.test.rule.ActivityTestRule;

import com.accloud.service.ACMsg;
import com.ilife.iliferobot.activity.LoginActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;

import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.Observer;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(JUnit4.class)
@LargeTest
public class LoginActivityTest {
    @Rule
    public ActivityTestRule<LoginActivity> loginRule = new ActivityTestRule<>(LoginActivity.class);

    public void setUp() {

    }

    @Test
    public void testSingle(){
        //有条件重新订阅
        Observable.just(2).repeatWhen(new Function<Observable<Object>, ObservableSource<?>>() {
            @Override
            public ObservableSource<?> apply(Observable<Object> objectObservable) throws Exception {
                //传递被观察者的事件
                //制定轮询时间，每一秒钟轮询一次
                //当发送error或者empty时间，轮询被终止
                // Observable.empty();
                //  Observable.error(new NullPointerException());
                return objectObservable.delay(1, TimeUnit.SECONDS);
            }
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }
            @Override
            public void onNext(Integer integer) {
                System.out.println("the value is"+integer);
            }
            @Override
            public void onError(Throwable e) {
            }
            @Override
            public void onComplete() {

            }
        });
    }

    @Test
    public void testLogin() {
        onView(withId(R.id.et_email)).perform(typeText("18565713334@163.com"), closeSoftKeyboard());
        onView(withId(R.id.et_pass)).perform(typeText("123456"), closeSoftKeyboard());
        onView(withId(R.id.bt_login)).perform(click()).check(matches(withText("登录")));
    }
}
