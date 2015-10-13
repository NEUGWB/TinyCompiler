package TinyCompiler;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class LexAnalys {
	public class LexElement {
		public String type;
		public String value;
	}
	
	private String fileName;
	private InputStream fr;
	private Set<String> keywordList = new HashSet<String>();
	private String curSymbol;
	char ch;

	LexElement curElement;

	public static String[] keyword = { "IF", "ELSE", "WHILE", "AND", "OR","DO",
			"NOT", "BEGIN", "END", "CALL", "SET", "FUNCTION", "PRINT", "EXIT" };

	public LexAnalys(String fileName) {
		this.fileName = fileName;
		keywordList.addAll(Arrays.asList(keyword));
	}
	
	public void init(){
		try {
			this.OpenFile();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		cg.adv();
		Advance();
	}

	class CharGetter {
		boolean end = false;
		boolean back = false;
		private int curPos = 0;
		private byte[] token = new byte[100 + 1];
		
		void zeroToken(){
			for (int i = 0; i < token.length; i++){
				token[i] = 0;
			}
		}
		
		void adv() {
			curPos++;
			if (token[curPos] == '\0') {
				int readLen = -1;
				try {
					zeroToken();
					readLen = fr.read(token);
				} catch (IOException e) {
					// e.printStackTrace();
					System.out.println("文件" + fileName + "读取失败");
				}
				if (readLen == -1) {
					end = true;
				} else {
					curPos = 0;
				}
			}
		}

		char get() {
			if (end) {
				return '#';
			}
			return (char) token[curPos];
		}
	}

	CharGetter cg = new CharGetter();

	public void OpenFile() throws FileNotFoundException {
		// fr = new FileReader(fileName);
		fr = System.in;
	}

	/*
	 * public void Advance(){ curPos++; if (token[curPos] == '\0') { int readLen
	 * = -1; try { readLen = fr.read(token); } catch (IOException e) { //
	 * e.printStackTrace(); System.out.println("文件" + fileName + "读取失败"); } if
	 * (readLen == -1) { end = true; } else { curPos = 0; } } }
	 * 
	 * public char GetCurChar(){ if (begin){ begin = false; Advance(); }
	 * 
	 * if (end){ return '#'; } return (char) token[curPos];
	 * 
	 * }
	 */

	/*
	 * public char GetChar() { if (curPos >= token.length || token[curPos] ==
	 * '\0') { int readLen = -1; try { for (int i = 0; i < token.length; i++){
	 * token[i] = 0; } readLen = fr.read(token); } catch (IOException e) { //
	 * e.printStackTrace(); System.out.println("文件" + fileName + "读取失败"); } if
	 * (readLen == -1) { end = true; return '#'; } else { curPos = 0; } } char
	 * ch = (char)token[curPos]; curPos++; return ch; }
	 */

	public boolean isKeyword(String symbol) {
		return keywordList.contains(symbol);
	}

	public boolean isOperator(char ch) {
		switch (ch) {
		case '+':
		case '-':
		case '*':
		case '/':
		case '(':
		case ')':
		case ',':
		case ';':
		case '<':
		case '>':
		case '=':
		case '!':
			return true;
		default:
			return false;
		}
	}

	public boolean isDigit(char ch) {
		return '0' <= ch && ch <= '9';
	}

	public boolean isLetter(char ch) {
		return ('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z');
	}

	public boolean isBlank(char ch) {
		return ch == ' ' || ch == '\t' || ch == '\r' || ch == '\n';
	}

	/*static enum state {
		START, ID, DIGIT, DECIMAL, OP_END, ID_END, DIGIT_END
	};*/

	public LexElement Start() {
		curSymbol = new String();
		while (true) {
			//
			ch = cg.get();
			if (isBlank(ch)) {
				cg.adv();
				continue;
			} else if (isDigit(ch)) {
				return DIGIT();
			} else if (isLetter(ch)) {
				return ID();
			} else if (isOperator(ch)) {
				return OP();
			} else {
				return null;
			}
		}
	}

	public LexElement ID() {
		curSymbol += ch;
		while (true) {
			cg.adv();
			ch = cg.get();
			if (isLetter(ch) || isDigit(ch)) {
				curSymbol += ch;
			} else {
				LexElement le = new LexElement();
				le.type = isKeyword(curSymbol) ? "keyword" : "id";
				le.value = curSymbol;
				// curPos--;
				return le;
			}
		}
	}

	public LexElement DIGIT() {
		curSymbol += ch;
		while (true) {
			cg.adv();
			ch = cg.get();
			if (isDigit(ch)) {
				curSymbol += ch;
			} else if (ch == '.') {
				return DEC_BEGIN();
			} else {
				LexElement le = new LexElement();
				le.type = "number";
				le.value = curSymbol;
				// curPos--;
				return le;
			}
		}
	}

	public LexElement DEC_BEGIN() {
		curSymbol += ch;
		cg.adv();
		ch = cg.get();
		if (isDigit(ch)) {
			return DECMAL();
		} else {
			return null;
		}
	}

	public LexElement DECMAL() {
		curSymbol += ch;
		while (true) {
			cg.adv();
			ch = cg.get();
			if (isDigit(ch)) {
				curSymbol += ch;
			} else {
				LexElement le = new LexElement();
				le.type = "number";
				le.value = curSymbol;
				// curPos--;
				return le;
			}
		}
	}

	public LexElement OP() {
		LexElement le = new LexElement();
		//le.type = "op";
		//le.value = curSymbol + ch;
		curSymbol += ch;
		switch (ch){
		case '<':
		case '>':
		case '!':
		case '=':{
			cg.adv();
			ch = cg.get();
			if (ch == '='){
				curSymbol += ch;
			} else{
				le.type = "op";
				le.value = curSymbol;
				return le;
			}
			
		}
		}
		cg.adv();
		le.type = "op";
		le.value = curSymbol;
		return le;
	}
	
	public void Advance(){
		if (cg.end) {
			curElement = null;
		} else if (cg.back) {
			cg.back = false;
		} else {
			curElement = Start();
		}
		//return curElement;
	}

	/*public LexElement GetNextSymbol() {
		// curElement = end ? null : Start();
		
		// return Start();
	}*/

	public LexElement GetCurrentSymbol() {
		return this.curElement;
	}

	public void Back() {
		cg.back = true;
	}

	public static void main(String args[]) {
		LexAnalys la = new LexAnalys("D:\\in.txt");
		// la.OpenFile();
		la.init();
		LexElement le;
		while ((le = la.GetCurrentSymbol()) != null) {
			System.out.println("<" + le.type + "," + le.value + ">");
			la.Advance();
		}
	}
}
