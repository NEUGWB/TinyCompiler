package TinyCompiler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import TinyCompiler.LexAnalys.LexElement;

public class Parser {
	class BoolElement extends Element {
		String trueCode;

		String falseCode;
		String id1name;

		String id2name;

		Object getValue() {
			return null;
		}
	}

	class Code {
		String dest;
		String op;
		String src1;
		String src2;

		@Override
		public String toString() {
			return String.format("%s\t%s\t%s\t%s", dest, op, src1, src2);
		}

	}

	abstract class Element {
		String name;
		String value;

		abstract Object getValue();
	}

	class FuncElement extends Element {

		List<Code> funCodeList = new ArrayList<Code>();

		Scope funScope = new Scope(GlobalScope);

		@Override
		Object getValue() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	class NumberElement extends Element {
		Object getValue() {
			return Double.parseDouble(value);
		}
	}

	class Scope {
		// List<Element> eTable = new ArrayList<Element>();
		SortedMap<String, Element> eTable = new TreeMap<String, Element>();

		Scope parent;

		Scope(Scope parent) {
			this.parent = parent;
		}
	}

	public static void main(String args[]) {
		Parser p = new Parser();
		p.la = new LexAnalys("input.txt");
		p.la.init();
		p.Parse();
	}

	private Set<String> defaultId = new HashSet<String>();

	private LexAnalys la;

	private int tempId = 0;

	private List<Code> globleCodeList = new ArrayList<Code>();

	private List<Code> curCodeList = globleCodeList;

	private boolean end = false;

	/*
	 * public boolean isKeyword(String symbol) { return
	 * keywordList.contains(symbol); }
	 */

	// private Set<String> keywordList = new HashSet<String>();

	private Scope GlobalScope = new Scope(null);
	private Scope curScope = GlobalScope;

	public Parser() {
		defaultId.addAll(Arrays.asList("cos", "sin", "tan", "sinh", "cosh", "tanh", "asin", "acos", "atan", "pow", "ln",
				"exp", "pi"));
	}

	private void addCode(String dest, String op, String src1, String src2) {
		Code c = new Code();
		c.dest = dest;
		c.op = op;
		c.src1 = src1;
		c.src2 = src2;
		curCodeList.add(c);
	}

	private void addElement(Scope sc, Element ele) {
		sc.eTable.put(ele.name, ele);
	}

	public void assign() {
		LexElement le = la.GetCurrentSymbol();
		la.Advance();
		le = la.GetCurrentSymbol();
		if (le.type.equals("id")) {
			Element lvalue = this.findElement(le.value);
			if (lvalue == null) {
				lvalue = new NumberElement();
				lvalue.name = le.value;
				addElement(curScope, lvalue);
			}
			la.Advance();
			// la.Advance();
			Element tmpE = Expression();
			// lvalue.value = tmpE.value;
			addCode(lvalue.name, "+", tmpE.name, null);
		}
	}

	private void blockState() {
		la.Advance();
		while (!la.GetCurrentSymbol().value.equals("END")) {
			Statement();
			la.Advance();
		}
	}

	private BoolElement BoolState() {
		LexElement le = la.GetCurrentSymbol();
		BoolElement be = new BoolElement();
		Element e1 = null, e2 = null;
		// if (le.type.equals("id") || le.type.equals("")){
		e1 = Factor();
		// }
		LexElement leop = la.GetCurrentSymbol();
		la.Advance();
		le = la.GetCurrentSymbol();
		// if (le.type.equals("id")){
		e2 = Factor();
		// }

		if (leop.type.equals("op")) {
			be.id1name = e1.name;
			be.id2name = e2.name;
			switch (leop.value) {
			case "<=":
				be.trueCode = "jbe";
				be.falseCode = "jo";
				break;
			case "<":
				be.trueCode = "jb";
				be.falseCode = "joe";
				break;
			case ">=":
				be.trueCode = "joe";
				be.falseCode = "jb";
				break;
			case ">":
				be.trueCode = "jo";
				be.falseCode = "jbe";
				break;
			case "==":
				be.trueCode = "je";
				be.falseCode = "jne";
				break;
			case "!=":
				be.trueCode = "jne";
				be.falseCode = "je";
				break;
			}
		}

		// la.Advance();
		return be;
	}

	private void defFunction() {
		la.Advance();
		LexElement le = la.GetCurrentSymbol();
		FuncElement eFun = new FuncElement();
		eFun.name = le.value;
		addElement(GlobalScope, eFun);

		curCodeList = eFun.funCodeList;
		curScope = eFun.funScope;

		// 解析参数列表
		la.Advance();
		if (la.GetCurrentSymbol().value.equals("(")) {
			la.Advance();
			// le = la.GetCurrentSymbol();
			while (true) {
				le = la.GetCurrentSymbol();
				if (le.type.equals("id")) {
					Element param = new NumberElement();
					param.name = le.value;
					addElement(curScope, param);
					la.Advance();
					le = la.GetCurrentSymbol();
					if (le.value.equals(",")) {
						la.Advance();
						continue;
					} else {
						error("参数列表缺少逗号");
					}
				} else if (le.value.equals(")")) {
					la.Advance();
					break;
				} else {
					error("参数列表缺少逗号");
				}
			}
		}

		Statement();

		curScope = GlobalScope;
		curCodeList = globleCodeList;
	}

	public void error(String info) {
		System.out.println(info);
	}

	public void exec() {
		Element d, s1, s2;
		for (int i = 0; i < globleCodeList.size();) {
			Code c = globleCodeList.get(i);
			d = findElement(c.dest);
			s1 = findElement(c.src1);
			s2 = findElement(c.src2);
			switch (c.op) {
			case "+": {
				Double res;
				if (s2 != null) {
					res = (Double) s1.getValue() + (Double) s2.getValue();
				} else {
					res = (Double) s1.getValue();
				}
				d.value = res.toString();
				if (d.name.equals("PRINT")) {
					System.out.println(d.value);
				}
				i++;
				break;
			}
			case "-": {
				/*
				 * Double res = (Double) s1.getValue() - (Double) s2.getValue();
				 * d.value = res.toString();
				 */

				Double res;
				if (s2 != null) {
					res = (Double) s1.getValue() - (Double) s2.getValue();
				} else {
					res = -(Double) s1.getValue();
				}
				d.value = res.toString();
				i++;
				break;
			}
			case "*": {
				Double res = (Double) s1.getValue() * (Double) s2.getValue();
				d.value = res.toString();
				i++;
				break;
			}
			case "/": {
				Double res = (Double) s1.getValue() / (Double) s2.getValue();
				d.value = res.toString();
				i++;
				break;
			}
			case "je": {
				if (NumbEqual((Double) s1.getValue(), (Double) s2.getValue())) {
					i = Integer.parseInt(c.dest);
				} else {
					i++;
				}
				break;
			}
			case "jne": {
				if (!NumbEqual((Double) s1.getValue(), (Double) s2.getValue())) {
					i = Integer.parseInt(c.dest);
				} else {
					i++;
				}
				break;
			}
			case "jb": {
				if ((Double) s1.getValue() < (Double) s2.getValue()) {
					i = Integer.parseInt(c.dest);
				} else {
					i++;
				}
				break;
			}
			case "jo": {
				if ((Double) s1.getValue() > (Double) s2.getValue()) {
					i = Integer.parseInt(c.dest);
				} else {
					i++;
				}
				break;
			}
			case "jbe": {
				if ((Double) s1.getValue() <= (Double) s2.getValue()) {
					i = Integer.parseInt(c.dest);
				} else {
					i++;
				}
				break;
			}
			case "joe": {
				if ((Double) s1.getValue() >= (Double) s2.getValue()) {
					i = Integer.parseInt(c.dest);
				} else {
					i++;
				}
				break;
			}
			case "jmp": {
				i = Integer.parseInt(c.dest);
				break;
			}
			}
		}
		// Element top = curScope.eTable.get(curScope.eTable.size() - 1);
		// System.out.println(top.value);
		if (curScope == GlobalScope) {
			globleCodeList.clear();

			Iterator<Entry<String, Element>> it = curScope.eTable.entrySet().iterator();
			while (it.hasNext()) {
				String name = it.next().getKey();
				if (name.startsWith("tmpvar")) {
					it.remove();
				}
			}
		}
	}

	private Element Expression() {
		LexElement le = la.GetCurrentSymbol();
		if (le.type.equals("op") && (le.value.equals("+") || le.value.equals("-"))) {
			la.Advance();
			Element tmpT = Term();
			Element tmpE = new NumberElement();
			tmpE.name = gennerTempVar();
			addElement(GlobalScope, tmpE);
			addCode(tmpE.name, le.value, tmpT.name, null);
			return tmpE;
		} else {
			Element tmpT = Term();
			// Element tmpT = new NumberElement();
			/*
			 * tmpT.name = gennerTempVar(); tmpT.value = ""+t;
			 * addElement(GlobalScope, tmpT);
			 */
			while (true) {
				/* LexElement */le = la.GetCurrentSymbol();
				if (le != null && (le.value.equals("+") || le.value.equals("-"))) {
					// return t + Expression();
					la.Advance();
					Element tmpT1 = Term();
					Element tmpE = new NumberElement();
					tmpE.name = gennerTempVar();
					// tmpE.value = ""+tmpT.value;
					addElement(GlobalScope, tmpE);
					addCode(tmpE.name, le.value, tmpT.name, tmpT1.name);
					tmpT = tmpE;
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
			return tmpT;
		}
		// }
		// return null;
	}

	private Element Factor() {
		LexAnalys.LexElement le = la.GetCurrentSymbol();
		if (le.type.equals("number")) { // F->number
			Element tmp = new NumberElement();
			tmp.name = gennerTempVar();
			tmp.value = le.value;
			addElement(GlobalScope, tmp); // 常数不需要code
			la.Advance();
			return tmp;
		} else if (le.type.equals("op") && le.value.equals("(")) { // F->(E)
			la.Advance();
			Element tmpE = Expression();
			// la.Advance();
			if (la.GetCurrentSymbol().value.equals(")")) {
				la.Advance();
				return tmpE;
			}
		} else if (le.type.equals("id")) { // F->id
			Element idfind = findElement(le.value);
			if (idfind != null) {
				la.Advance();
				return idfind;
			} else {
				error("变量未找到：" + le.value);
			}
		}
		return null;
	}

	public Element findElement(String name) {
		if (name == null) {
			return null;
		}

		return curScope.eTable.get(name);
		// return null;
	}

	private String gennerTempVar() {
		String name = "tmpvar";
		name += tempId;
		this.tempId++;
		return name;
	}

	private void ifstate() {
		la.Advance();
		BoolElement E = BoolState();
		addCode(null, E.falseCode, E.id1name, E.id2name);
		int jCode = globleCodeList.size() - 1;

		// la.Advance();
		if (!la.GetCurrentSymbol().value.equals("THEN")) {
			error("IF语句缺少THEN");
		}
		la.Advance();
		Statement();
		la.Advance();
		int sEnd = globleCodeList.size();
		if (la.GetCurrentSymbol().value.equals("ELSE")) {
			addCode(null, "jmp", null, null);
			int ejCode = globleCodeList.size() - 1;
			sEnd++;

			la.Advance();
			Statement();
			la.Advance();
			int elsEnd = globleCodeList.size();
			globleCodeList.get(ejCode).dest = "" + elsEnd;
		}
		// int sEnd = codeList.size();

		if (!la.GetCurrentSymbol().value.equals("END")) {
			error("IF语句缺少END");
		}
		globleCodeList.get(jCode).dest = "" + sEnd;
	}

	private boolean NumbEqual(Double d1, Double d2) {
		return Math.abs(d1 - d2) < 0.0000001;
	}

	void Parse() {
		// return Expression();
		NumberElement PRINT = new NumberElement();
		PRINT.name = "PRINT";
		addElement(GlobalScope, PRINT);
		while (!end) {
			Statement();
			// exec();
			la.Advance();
		}

		for (Code e : this.curCodeList) {
			System.out.println(e.toString());
		}
		System.out.println("******************");
		for (Map.Entry<String, Element> e : this.curScope.eTable.entrySet()) {
			System.out.println(e.getKey() + ' ' + e.getValue().name + ' ' + e.getValue().value);
		}
	}

	private void PrintCode() {
		for (Code c : this.globleCodeList) {
			System.out.println(c.dest + " " + c.op + " " + c.src1 + " " + c.src2);
		}
	}

	private void PrintElement() {
		/*
		 * for (Element e : this.GlobalScope.eTable) {
		 * System.out.println(e.getClass().getName() + " " + e.name + " " +
		 * e.value); }
		 */
		for (Entry<String, Element> e : this.curScope.eTable.entrySet()) {
			Element value = e.getValue();

			System.out.println(e.getClass().getName() + " " + value.name + " " + value.value);
		}
	}

	void Statement() {
		LexElement le = la.GetCurrentSymbol();
		if (le == null) {
			this.end = true;
			return;
		}
		if (le.type == "keyword") {
			switch (le.value) {
			case "PRINT": {
				la.Advance();
				Element tmpE = Expression();
				le = la.GetCurrentSymbol();
				if (!le.value.equals("END")) {
					error("不正常的结束符" + le.value);
				}
				addCode("PRINT", "+", tmpE.name, null);
				// exec();
				// System.out.println(tmpE.value);
				break;
			}
			case "BEGIN": {
				blockState();
				break;
			}
			case "SET": {
				assign();
				break;
			}
			case "IF": {
				ifstate();
				break;
			}
			case "WHILE": {
				whileState();
				break;
			}
			case "FUNCTION": {
				defFunction();
			}
			case "EXIT":
				return;
			}
		}
	}

	private Element Term() {
		/*
		 * double f = Factor(); if (la.GetNextSymbol().value.equals("*")){
		 * return f + Term(); } else { return f; }
		 */
		Element tmpF = Factor();
		while (true) {
			LexElement le = la.GetCurrentSymbol();
			if (le != null && (le.value.equals("*") || le.value.equals("/"))) {
				// return t + Expression();
				la.Advance();
				Element tmpF1 = Factor();
				Element tmpT = new NumberElement();
				tmpT.name = gennerTempVar();
				// tmpE.value = ""+tmpT.value;
				addElement(GlobalScope, tmpT);
				addCode(tmpT.name, le.value, tmpF.name, tmpF1.name);

				tmpF = tmpT;
			} else {
				// la.Back();
				break;
			}
			// return tmpE;
		}

		return tmpF;
	}

	private void whileState() {
		la.Advance();
		int whBegin = globleCodeList.size(); // 放在BoolState之前表示判断前可能有计算，如a+b<3
		BoolElement be = BoolState();
		addCode(null, be.falseCode, be.id1name, be.id2name);
		int jCode = globleCodeList.size() - 1;
		if (!la.GetCurrentSymbol().value.equals("DO")) {
			error("WHILE语句缺少DO");
		}
		la.Advance();
		Statement();
		la.Advance();
		addCode("" + whBegin, "jmp", null, null);

		if (!la.GetCurrentSymbol().value.equals("END")) {
			error("WHILE语句缺少END");
		}

		int sEnd = globleCodeList.size();
		globleCodeList.get(jCode).dest = "" + sEnd;
	}
}

