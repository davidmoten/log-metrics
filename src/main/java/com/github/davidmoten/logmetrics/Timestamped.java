package com.github.davidmoten.logmetrics;

public class Timestamped<T> implements Comparable<Timestamped<T>> {
    private final long time;
    private final T value;

    public Timestamped(long time, T value) {
	this.time = time;
	this.value = value;
    }

    public long getTime() {
	return time;
    }

    public T getValue() {
	return value;
    }

    @Override
    public int compareTo(Timestamped<T> o) {
	return ((Long) time).compareTo(o.time);
    }

    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result + (int) (time ^ (time >>> 32));
	result = prime * result + ((value == null) ? 0 : value.hashCode());
	return result;
    }

    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (getClass() != obj.getClass())
	    return false;
	Timestamped<?> other = (Timestamped<?>) obj;
	if (time != other.time)
	    return false;
	if (value == null) {
	    if (other.value != null)
		return false;
	} else if (!value.equals(other.value))
	    return false;
	return true;
    }

}
