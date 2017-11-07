package searchAlgorithms;

/**
 * Enumeration for the different types of algorithms
 *
 */
public enum Method {

	SHORTEST(0), FASTEST(1), SMARTEST(2);
	
    public final int value;

    private Method(int value) {
        this.value = value;
    }
}