package shaper.mapping.model.r2rml;

import janus.database.SQLSelectField;

import java.util.Optional;

public abstract class TermMap {

    public enum TermTypes {
        IRI, BLANKNODE, LITERAL
    }

    private Optional<String> constant; // rr:constant //  IRI in subject map
    private Optional<SQLSelectField> column; // rr:column
    private Optional<Template> template; // rr:template
    private Optional<TermTypes> termType; // rr:termType
    private Optional<String> language; // rr:language
    private Optional<String> datatype; // rr:datatype
    private Optional<String> inverseExpression; // rr:inverseExpression

    TermMap() {
        constant = Optional.empty();
        column = Optional.empty();
        template = Optional.empty();
        termType = Optional.empty();
        language = Optional.empty();
        datatype = Optional.empty();
        inverseExpression = Optional.empty();
    }

    public void setConstant(String constant) { this.constant = Optional.ofNullable(constant); }
    public void setColumn(SQLSelectField column) { this.column = Optional.ofNullable(column); }
    public void setTemplate(Template template) { this.template = Optional.ofNullable(template); }
    public void setTermType(TermTypes termType) { this.termType = Optional.ofNullable(termType); }
    public void setLanguage(String language) { this.language = Optional.ofNullable(language); }
    public void setDatatype(String datatype) { this.datatype = Optional.ofNullable(datatype); }
    public void setinverseExpression(String inverseExpression) { this.inverseExpression = Optional.ofNullable(inverseExpression); }

    public Optional<Template> getTemplate() { return template; }
    public Optional<TermTypes> getTermType() { return termType; }
    public Optional<String> getConstant() { return constant; }
    public Optional<String> getLanguage() { return language; }
    public Optional<String> getDatatype() { return datatype; }
    public Optional<SQLSelectField> getColumn() { return column; }
}
