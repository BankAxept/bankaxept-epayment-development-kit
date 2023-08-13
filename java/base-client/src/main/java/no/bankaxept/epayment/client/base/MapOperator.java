package no.bankaxept.epayment.client.base;

import java.util.concurrent.Flow;
import java.util.function.Function;

public class MapOperator<T, U> implements Flow.Processor<T, U> {

  private final Flow.Publisher<T> upstream;
  private final Function<T, U> mapper;
  private Flow.Subscriber<? super U> downstream;

  public MapOperator(Flow.Publisher<T> publisher, Function<T, U> mapper) {
    this.upstream = publisher;
    this.mapper = mapper;
  }

  @Override
  public void subscribe(Flow.Subscriber<? super U> subscriber) {
    this.downstream = subscriber;
    upstream.subscribe(this);
  }

  @Override
  public void onSubscribe(Flow.Subscription subscription) {
    downstream.onSubscribe(subscription);
  }

  @Override
  public void onNext(T item) {
    U mappedItem;
    try {
      mappedItem = mapper.apply(item);
    } catch (Throwable t) {
      onError(t);
      return;
    }
    downstream.onNext(mappedItem);
  }

  @Override
  public void onError(Throwable throwable) {
    downstream.onError(throwable);
  }

  @Override
  public void onComplete() {
    downstream.onComplete();
  }

}
