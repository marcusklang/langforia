package se.lth.cs.nlp.langforia.kernel.exceptions;

public class LangforiaRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 2135751860728029238L;

	private String language = null;
	
	public LangforiaRuntimeException() {
		super();
	}

	public LangforiaRuntimeException(String message, Throwable cause) {
		super(message, cause);
	}

	public LangforiaRuntimeException(String message) {
		super(message);
	}

	public LangforiaRuntimeException(Throwable cause) {
		super(cause);
	}

	public LangforiaRuntimeException(String lang, String message, Throwable cause) {
		super(message, cause);
		this.language = lang;
	}

	public LangforiaRuntimeException(String lang, String message) {
		super(message);
		this.language = lang;
	}

	public LangforiaRuntimeException(Throwable cause, String lang) {
		super(cause);
		this.language = lang;
	}
	
	public String getLanguage() {
		return language;
	}
	
	@Override
	public String toString() {
		if(language == null)
			return super.toString();
		else
			return "[" + language + "] " + super.toString();
	}
}
