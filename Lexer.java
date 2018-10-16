import java.util.Scanner;

/*
 * 文字列から数値を読み取るプログラム
 *   0                           -> 整数 (例: 0)
 *   [1-9][0-9]*                 -> 整数 (例: 100)
 *   0[xX][0-9a-fA-F]+           -> 整数 (例: 0xabc)
 *   [0-9]*[a-fA-F][0-9a-fA-F]*  -> 整数 (例: 0123456789a)
 *   [1-9][0-9]*\.[0-9]*         -> 小数 (例: 10.3)
 *   0\.[0-9]*                   -> 小数 (例: 0.12)
 *   \.[0-9]+                    -> 小数 (例: .12)
 */

public class Lexer {
	static class Token {
		static final String TYPE_INT = "INT";
		static final String TYPE_DEC = "DEC";
		static final String TYPE_ERR = "ERR";
		
		Token(String tokenType, int start, int len) {
			this.tokenType = tokenType;
			this.start = start;
			this.len = len;
		}
		
		String tokenType;  /* トークンの種類 */
		int start;         /* 文字列中のトークン開始位置 */
		int len;           /* トークンの長さ */
	}
	
	static final int CT_P = 0;
	static final int CT_X = 1;
	static final int CT_0 = 2;
	static final int CT_1 = 3;
	static final int CT_A = 4;
	static final int CT_OTHER = 5;

	/*
	 * 文字を分類する
	 *   [1-9] や [a-f] をまとめて扱えるようにするため．
	 */
	static int getCharType(int c) {
		if (c == '.')             return CT_P;
		if (c == 'x' || c == 'X') return CT_X;
		if (c == '0')             return CT_0;
		if ('1' <= c && c <= '9') return CT_1;
		if ('a' <= c && c <= 'f') return CT_A;
		if ('A' <= c && c <= 'F') return CT_A;
		return CT_OTHER;
	}
	
	int[][] delta = {
		/* TODO */
		/* 状態遷移表を作る */
		/*   delta[現状態][入力記号] */

	    /*  P   X   0   1   A   OTHER */
	    {   4,  3,  2,  1,  1,  3 },   /* 0 _初期状態 */
	    {   4, 10,  1,  1,  1, 10 },   /* 1 _10進数  */
	    {   6,  5,  2,  8,  1, 10 },   /* 2 _初期値0 */
	    {   3,  3,  3,  3,  3,  3 },   /* 3 _ERROR  */
	    {   3,  3,  7,  7,  3,  3 },   /* 4 _初期値小数点 */
	    {  10, 10, 12, 12, 12, 10 },  /* 5 _16進数 */
	    {  10, 10, 11, 11, 10, 10 },  /* 6 _   */
	    {   9,  9,  7,  7,  9,  9 },  /* 7 _　 */
	    {  10, 10,  8,  8,  1, 10 },  /* 8 _   */
	    {   9,  9,  9,  9,  9,  9 },  /* 9 _実数状態 */
	    {  10, 10, 10, 10, 10, 10 },  /*10 _10進数状態 */
	    {   9,  9, 11, 11,  9,  9 },   /*11  */
	    {  10, 10, 12, 12, 12, 10 }   /*12  */ 
	    
	};

	/*
	 * 文字列 str の start 文字目から字句解析しトークンを一つ返す
	 */
	Token getToken(String str, int start) {
		/* 現在注目している文字 (先頭から p 文字目)  */
		int p = start;

		/* 最後の受理状態のマーカとその時何文字目まで読んだか */
		String acceptMarker = Token.TYPE_ERR;
		int acceptPos = start;

		/* 現在の状態 */
		int currentState = 0;

		while (p < str.length()) {
			int c = str.charAt(p); /* str の p 文字目を読み取る */
			p++;
			
			int ct = getCharType(c);
			int nextState = delta[currentState][ct];

			/* TODO */
			/* 行先がなければループを抜ける */
			/* 行先が受理状態であれば「最後の受理状態」を更新する */
			
			if(nextState == 3){ /* ERRの受理状態 */
			    acceptMarker= Token.TYPE_ERR;
			    acceptPos = start;
			    break;
			    
			}else if(nextState == 9){ /* 小数の受理状態 */
			    acceptMarker = Token.TYPE_DEC;
			    break;
			    
			}else if(nextState == 10){ /* 整数の受理状態 */
			    acceptMarker = Token.TYPE_INT;
			    if(currentState == 5) {
				acceptPos = 1;
			    }
			    break;
			    
			}else if (nextState == 1 || nextState == 2 || nextState == 6) { /* 整数の受理状態(行き先なし終了) */
			    acceptMarker = Token.TYPE_INT;
			    acceptPos++;

			}else if (nextState == 7 || nextState == 11) { /* 小数の受理状態(行き先なし終了) */
			    acceptMarker = Token.TYPE_DEC;
			    acceptPos++;
			    
			}else if (nextState == 5){ /* 0xスタート */
			    acceptMarker = Token.TYPE_INT;
			    acceptPos++;
			    if(str.length() == 2) { /* 0xのみの状態 */
				acceptPos--;
			    }	
			    
			}else if (nextState == 8){ /* 0スタート */
			    acceptPos++;
			    if(currentState == 8 && (str.length() ==  acceptPos)) { /* 0の後は全て数字の状態 */
				acceptPos = 1;
			    }
    
			}else if (nextState == 4 && str.length() == 1) { /* 小数点のみの状態 */
			    acceptMarker= Token.TYPE_ERR;
			
			}else{
			    acceptPos++;
			}
			currentState = nextState;
		}
		
		return new Token(acceptMarker, start, acceptPos - start);
	}
	
	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		String str = sc.nextLine();  /* 1行読み取る */
		Lexer lex = new Lexer();
		Token t = lex.getToken(str, 0);
		System.out.print(t.tokenType);
		System.out.println(str.substring(t.start, t.start + t.len));
	}
}
