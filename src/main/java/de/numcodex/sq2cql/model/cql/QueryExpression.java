package de.numcodex.sq2cql.model.cql;

import de.numcodex.sq2cql.PrintContext;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

public record QueryExpression(SourceClause sourceClause, List<QueryInclusionClause> queryInclusionClauses,
                              WhereClause whereClause,
                              ReturnClause returnClause) implements Expression<QueryExpression> {

    public QueryExpression {
        requireNonNull(sourceClause);
        queryInclusionClauses = List.copyOf(queryInclusionClauses);
        requireNonNull(whereClause);
    }

    public static QueryExpression of(SourceClause sourceClause) {
        return new QueryExpression(sourceClause, List.of(), WhereClause.of(Expression.TRUE), null);
    }

    public static QueryExpression of(SourceClause sourceClause, WhereClause whereClause) {
        return new QueryExpression(sourceClause, List.of(), whereClause, null);
    }

    public static QueryExpression of(SourceClause sourceClause, ReturnClause returnClause) {
        return new QueryExpression(sourceClause, List.of(), WhereClause.of(Expression.TRUE), returnClause);
    }

    public QueryExpression appendQueryInclusionClause(QueryInclusionClause clause) {
        var clauses = new LinkedList<>(this.queryInclusionClauses);
        clauses.add(clause);
        return new QueryExpression(sourceClause, clauses, whereClause, returnClause);
    }

    public QueryExpression updateWhereClauseExpr(Function<DefaultExpression, DefaultExpression> mapper) {
        return new QueryExpression(sourceClause, queryInclusionClauses, whereClause.map(mapper), returnClause);
    }

    public IdentifierExpression sourceAlias() {
        return sourceClause.source().alias();
    }

    @Override
    public String print(PrintContext printContext) {
        var clauses = clauses();
        var context = printContext.increase().resetPrecedence();
        return clauses.size() == 1
                ? sourceClause.source().querySource().print(context)
                : printContext.parenthesizeZero(clauses.stream().map(context::print).collect(joining("\n" + context.getIndent())));
    }

    @Override
    public QueryExpression withIncrementedSuffixes(Map<String, Integer> increments) {
        //TODO: decent into clauses
        return this;
    }

    private List<Clause> clauses() {
        var builder = Stream.<Clause>builder();
        builder.add(sourceClause);
        queryInclusionClauses.forEach(builder);
        if (whereClause.expression() != Expression.TRUE) builder.add(whereClause);
        if (returnClause != null) builder.add(returnClause);
        return builder.build().toList();
    }
}
