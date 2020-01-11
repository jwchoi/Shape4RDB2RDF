package shaper.mapping.model.rdf;

@SuppressWarnings("serial")
class IndividualParsingException extends Exception {

	@Override
	public String getMessage() {
		return "Can't Parse Individual.";
	}
}
