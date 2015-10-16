package TinyCompiler;

import java.util.ArrayList;
import java.util.List;

import TinyCompiler.LexAnalys.LexElement;
import TinyCompiler.Parser.Element;
import TinyCompiler.Parser.NumberElement;

public class Parser2 {
	public Parser2() {
		la = new LexAnalys("input.txt");
		la.init();
	}

	private LexAnalys la;

	private ExpressionNode expression() {
		ExpressionNode expr = new ExpressionNode();
		expr.childs = new ArrayList<ExpressionNode>();
		expr.name = "EXPR";

		LexElement le = la.GetCurrentSymbol();
		if (le.type.equals("op") && (le.value.equals("+") || le.value.equals("-"))) {
			/*
			 * la.Advance(); ExpressionNode tmpT = term(); Element tmpE = new
			 * NumberElement(); tmpE.name = gennerTempVar();
			 * addElement(GlobalScope, tmpE); addCode(tmpE.name, le.value,
			 * tmpT.name, null); return tmpE;
			 */
			ExpressionNode op = new ExpressionNode();
			op.name = "op" + le.value;
			expr.childs.add(op);
			op.childs.add(expression());
		} else {
			ExpressionNode term = term();
			// Element tmpT = new NumberElement();
			/*
			 * tmpT.name = gennerTempVar(); tmpT.value = ""+t;
			 * addElement(GlobalScope, tmpT);
			 */
			expr.childs.add(term);
			while (true) {
				/* LexElement */le = la.GetCurrentSymbol();
				if (le != null && (le.value.equals("+") || le.value.equals("-"))) {
					// return t + Expression();
					la.Advance();
					/*
					 * Element tmpT1 = term(); Element tmpE = new
					 * NumberElement(); tmpE.name = gennerTempVar(); //
					 * tmpE.value = ""+tmpT.value; addElement(GlobalScope,
					 * tmpE); addCode(tmpE.name, le.value, tmpT.name,
					 * tmpT1.name); tmpT = tmpE;
					 */
					ExpressionNode op = new ExpressionNode();
					op.name = "op" + le.value;
					expr.childs.add(op);
					op.childs.add(expression());
				} /*
					 * else if (le.value.equals(";")) { return tmpT; }
					 */else {
					// la.Back();
					break;
				}
				// return tmpE;
			} // else{
			/*
			 * Element tmpE = new NumberElement(); tmpE.name = gennerTempVar();
			 * //tmpE.value = ""+tmpT.value; addElement(GlobalScope, tmpE);
			 * addCode(tmpE.name, "+", tmpT.name, null); return tmpE;
			 */
			// return t;
			return expr;
		}
	}

	private ExpressionNode term() {
		ExpressionNode term = new ExpressionNode();
		term.childs = new ArrayList<ExpressionNode>();
		term.name = "TERM";

		LexElement le = la.GetCurrentSymbol();

		ExpressionNode fact = factor();

		term.childs.add(fact);
		while (true) {
			le = la.GetCurrentSymbol();
			if (le != null && (le.value.equals("*") || le.value.equals("/"))) {
				la.Advance();

				ExpressionNode op = new ExpressionNode();
				op.name = "op" + le.value;
				term.childs.add(op);
				op.childs.add(term());
			} else {
				break;
			}
		}

		return term;

	}

	private ExpressionNode factor() {
		return null;
	}
}

class SyntaxTree {
	SyntaxTreeNode root;
}

class SyntaxTreeNode {

}

class ExpressionNode extends SyntaxTreeNode {
	String name;
	List<ExpressionNode> childs;
}
