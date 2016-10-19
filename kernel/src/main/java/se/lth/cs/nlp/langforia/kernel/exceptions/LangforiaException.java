package se.lth.cs.nlp.langforia.kernel.exceptions;

public class LangforiaException extends Exception {

	private static final long serialVersionUID = -2235803178472156711L;
	
	private String language = null;
	
	public LangforiaException() {
		super();
	}

	public LangforiaException(String message, Throwable cause) {
		super(message, cause);
	}

	public LangforiaException(String message) {
		super(message);
	}

	public LangforiaException(Throwable cause) {
		super(cause);
	}
	
	public LangforiaException(String lang, String message, Throwable cause) {
		super(message, cause);
		this.language = lang;
	}

	public LangforiaException(String lang, String message) {
		super(message);
		this.language = lang;
	}

	public LangforiaException(Throwable cause, String lang) {
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
