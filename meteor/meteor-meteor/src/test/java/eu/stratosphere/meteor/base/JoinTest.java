package eu.stratosphere.meteor.base;

import org.junit.Test;

import eu.stratosphere.meteor.MeteorTest;
import eu.stratosphere.sopremo.ExpressionTag;
import eu.stratosphere.sopremo.JsonUtil;
import eu.stratosphere.sopremo.Sink;
import eu.stratosphere.sopremo.SopremoPlan;
import eu.stratosphere.sopremo.Source;
import eu.stratosphere.sopremo.base.Join;
import eu.stratosphere.sopremo.expressions.ComparativeExpression;
import eu.stratosphere.sopremo.expressions.ComparativeExpression.BinaryOperator;
import eu.stratosphere.sopremo.expressions.EvaluationExpression;
import eu.stratosphere.sopremo.expressions.InputSelection;
import eu.stratosphere.sopremo.expressions.ObjectAccess;
import eu.stratosphere.sopremo.expressions.ObjectCreation;
import eu.stratosphere.sopremo.expressions.PathExpression;

public class JoinTest extends MeteorTest {

	@Test
	public void testJoin1() {
		final SopremoPlan actualPlan = this.parseScript("$users = read from 'users.json';\n" +
			"$pages = read from 'pages.json';\n" +
			"$result = join $users, $pages\n" +
			"  where $users.id == $pages.userid\n" +
			"  into { $users.name, $pages.* };\n" +
			"write $result to 'result.json';");

		final SopremoPlan expectedPlan = new SopremoPlan();
		final Source users = new Source("users.json");
		final Source pages = new Source("pages.json");
		final Join join = new Join().
			withInputs(users, pages).
			withJoinCondition(new ComparativeExpression(JsonUtil.createPath("0", "id"),
				BinaryOperator.EQUAL, JsonUtil.createPath("1", "userid"))).
			withResultProjection(new ObjectCreation(
				new ObjectCreation.FieldAssignment("name", JsonUtil.createPath("0", "name")),
				new ObjectCreation.CopyFields(JsonUtil.createPath("1"))
				));
		final Sink result = new Sink("result.json").withInputs(join);
		expectedPlan.setSinks(result);

		assertEquals(expectedPlan, actualPlan);
	}

	@Test
	public void testJoin2() {
		final SopremoPlan actualPlan = this.parseScript("$users = read from 'users.json';\n" +
			"$pages = read from 'pages.json';\n" +
			"$result = join $u in $users, $p in $pages\n" +
			"  where $u.id == $p.userid\n" +
			"  into { $u.name, $p.* };\n" +
			"write $result to 'result.json';");

		final SopremoPlan expectedPlan = new SopremoPlan();
		final Source users = new Source("users.json");
		final Source pages = new Source("pages.json");
		final Join join = new Join().
			withInputs(users, pages).
			withJoinCondition(new ComparativeExpression(JsonUtil.createPath("0", "id"),
				BinaryOperator.EQUAL, JsonUtil.createPath("1", "userid"))).
			withResultProjection(new ObjectCreation(
				new ObjectCreation.FieldAssignment("name", JsonUtil.createPath("0", "name")),
				new ObjectCreation.CopyFields(JsonUtil.createPath("1"))
				));
		final Sink result = new Sink("result.json").withInputs(join);
		expectedPlan.setSinks(result);

		assertEquals(expectedPlan, actualPlan);
	}

	@Test
	public void testOuterJoin() {
		final SopremoPlan actualPlan = this.parseScript("$users = read from 'users.json';\n" +
			"$pages = read from 'pages.json';\n" +
			"$result = join preserve $u in $users, $p in $pages\n" +
			"  where $u.id == $p.userid\n" +
			"  into { $u.name, $p.* };\n" +
			"write $result to 'result.json';");

		final EvaluationExpression retainFirst = new InputSelection(0).withTag(ExpressionTag.RETAIN);
		final SopremoPlan expectedPlan = new SopremoPlan();
		final Source users = new Source("users.json");
		final Source pages = new Source("pages.json");
		final Join join = new Join().
			withInputs(users, pages).
			withJoinCondition(new ComparativeExpression(new PathExpression(retainFirst, new ObjectAccess("id")),
				BinaryOperator.EQUAL, JsonUtil.createPath("1", "userid"))).
			withResultProjection(new ObjectCreation(
				new ObjectCreation.FieldAssignment("name", new PathExpression(retainFirst, new ObjectAccess("name"))),
				new ObjectCreation.CopyFields(JsonUtil.createPath("1"))
				));
		final Sink result = new Sink("result.json").withInputs(join);
		expectedPlan.setSinks(result);

		assertEquals(expectedPlan, actualPlan);
	}
}
