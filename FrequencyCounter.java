import java.math.BigInteger;

public abstract interface FrequencyCounter
{
    public abstract BigInteger getFrequencyCount(String token, String tag);
    public abstract BigInteger getTotalCount();
}
