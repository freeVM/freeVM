/* Generated By:JavaCC: Do not edit this line. CSSParser.java */
package org.apache.harmony.x.swing.text.html.cssparser;

import org.apache.harmony.x.swing.text.html.cssparser.metamodel.*;
import org.apache.harmony.x.swing.internal.nls.Messages;

public class CSSParser implements CSSParserConstants {
    private final CSSLogger logger = new CSSLogger();

    public Sheet parse() throws ParseException {
         return cssGrammar();
    }

    public RuleSet parseRuleSet() throws ParseException {
        return ruleSet_recoverable();
    }


    private boolean skipTillDeclarationEnd() {
        logger.logError(getToken(1));
        return skipTillTokens(new int[] {CLOSE_BRACE, SEMICOLON}, false);
    }

    private boolean skipTillImportEnd() {
        logger.logError(getToken(1));
        return skipTillTokens(new int[] {SEMICOLON}, true);
    }

    private boolean skipTillRuleSetEnd() {
        logger.logError(getToken(1));
        return skipTillTokens(new int[] {CLOSE_BRACE}, true);
    }


    private boolean skipTillTokens(final int[] stopTokens, final boolean removeStopToken) {
        Token currentToken = getToken(1);
        while (currentToken != null && currentToken.kind != EOF) {
            for (int i = 0; i < stopTokens.length; i++) {
                if (currentToken.kind == stopTokens[i]) {
                    if (removeStopToken) {
                        getNextToken();
                    }
                    return true;
                }
            }
            getNextToken();
            currentToken = getToken(1);
        }
        return false;
    }

  final public Sheet cssGrammar() throws ParseException {
 Sheet result;
    result = styleSheet();
    jj_consume_token(0);
    {if (true) return result;}
    throw new Error(Messages.getString("swing.err.15")); //$NON-NLS-1$
  }

  final public Sheet styleSheet() throws ParseException {
  Sheet result = new Sheet(); String importURL;
    label_1:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IMPORT:
        ;
        break;
      default:
        jj_la1[0] = jj_gen;
        break label_1;
      }
      importURL = import_recoverable();
                                       result.addImport(importURL);
    }
    styleSheetBody(result);
      {if (true) return result;}
    throw new Error(Messages.getString("swing.err.15")); //$NON-NLS-1$
  }

  final public String importStatement() throws ParseException {
  Token importValue;
    jj_consume_token(IMPORT);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case STRING:
      importValue = jj_consume_token(STRING);
      break;
    case URL:
      importValue = jj_consume_token(URL);
      break;
    default:
      jj_la1[1] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
    jj_consume_token(SEMICOLON);
        {if (true) return TokenResolver.resolve(importValue);}
    throw new Error(Messages.getString("swing.err.15")); //$NON-NLS-1$
  }

  final public String import_recoverable() throws ParseException {
  String result = null;
    try {
      result = importStatement();
    } catch (ParseException e) {
      skipTillImportEnd();
    }
      {if (true) return result;}
    throw new Error(Messages.getString("swing.err.15")); //$NON-NLS-1$
  }

  final public void styleSheetBody(final Sheet sheet) throws ParseException {
  RuleSet ruleSet;
    label_2:
    while (true) {
      if (getToken(1).kind != EOF) {
        ;
      } else {
        break label_2;
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case HEX_COLOR:
      case IDENT:
      case ELEMENT_NAME:
      case SOLITARY_ID:
      case SOLITARY_CLASS:
      case SOLITARY_PSEUDO_CLASS:
      case SOLITARY_PSEUDO_ELEMENT:
      case HASH:
        ruleSet = ruleSet_recoverable();
                                                                            sheet.addRuleSet(ruleSet);
        break;
      default:
        jj_la1[2] = jj_gen;
        skipTillExpressionEnd();
      }
    }
  }

  final public void mediaSet() throws ParseException {
    jj_consume_token(MEDIA);
    jj_consume_token(IDENT);
    jj_consume_token(OPEN_BRACE);
    label_3:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case HEX_COLOR:
      case IDENT:
      case ELEMENT_NAME:
      case SOLITARY_ID:
      case SOLITARY_CLASS:
      case SOLITARY_PSEUDO_CLASS:
      case SOLITARY_PSEUDO_ELEMENT:
      case HASH:
        ;
        break;
      default:
        jj_la1[3] = jj_gen;
        break label_3;
      }
      ruleSet_recoverable();
    }
    jj_consume_token(CLOSE_BRACE);
  }

  final public RuleSet ruleSet() throws ParseException {
  RuleSet result = new RuleSet(); String selector; Property p;
    selector = selector();
                           result.addSelector(selector);
    label_4:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case COMMA:
        ;
        break;
      default:
        jj_la1[4] = jj_gen;
        break label_4;
      }
      jj_consume_token(COMMA);
      selector = selector();
                                                                                            result.addSelector(selector);
    }
    jj_consume_token(OPEN_BRACE);
    p = declaration_recoverable();
                                  result.addProperty(p);
    label_5:
    while (true) {
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case SEMICOLON:
        ;
        break;
      default:
        jj_la1[5] = jj_gen;
        break label_5;
      }
      jj_consume_token(SEMICOLON);
      p = declaration_recoverable();
                                                                                                      result.addProperty(p);
    }
    jj_consume_token(CLOSE_BRACE);
     {if (true) return result;}
    throw new Error(Messages.getString("swing.err.15")); //$NON-NLS-1$
  }

  final public RuleSet ruleSet_recoverable() throws ParseException {
  RuleSet result = null;
    try {
      result = ruleSet();
    } catch (ParseException pe) {
     skipTillRuleSetEnd();
    }
     {if (true) return result;}
    throw new Error(Messages.getString("swing.err.15")); //$NON-NLS-1$
  }

  final public String selector() throws ParseException {
  Token startToken = getToken(1);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case HEX_COLOR:
    case IDENT:
    case ELEMENT_NAME:
    case SOLITARY_ID:
    case SOLITARY_CLASS:
    case SOLITARY_PSEUDO_CLASS:
    case HASH:
      label_6:
      while (true) {
        simple_selector();
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case HEX_COLOR:
        case IDENT:
        case ELEMENT_NAME:
        case SOLITARY_ID:
        case SOLITARY_CLASS:
        case SOLITARY_PSEUDO_CLASS:
        case HASH:
          ;
          break;
        default:
          jj_la1[6] = jj_gen;
          break label_6;
        }
      }
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case PSEUDO_ELEMENT:
      case SOLITARY_PSEUDO_ELEMENT:
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case PSEUDO_ELEMENT:
          pseudo_element();
          break;
        case SOLITARY_PSEUDO_ELEMENT:
          solitary_pseudo_element();
          break;
        default:
          jj_la1[7] = jj_gen;
          jj_consume_token(-1);
          throw new ParseException();
        }
        break;
      default:
        jj_la1[8] = jj_gen;
        ;
      }
      break;
    case SOLITARY_PSEUDO_ELEMENT:
      solitary_pseudo_element();
      break;
    default:
      jj_la1[9] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
     {if (true) return TokenResolver.resolve(startToken, getToken(0), true);}
    throw new Error(Messages.getString("swing.err.15")); //$NON-NLS-1$
  }

  final public void simple_selector() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IDENT:
    case ELEMENT_NAME:
      element_name();
      break;
    case HEX_COLOR:
    case SOLITARY_ID:
    case HASH:
      solitary_id();
      break;
    case SOLITARY_CLASS:
      solitary_class();
      break;
    case SOLITARY_PSEUDO_CLASS:
      solitary_pseudo_class();
      break;
    default:
      jj_la1[10] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void element_name() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case ELEMENT_NAME:
      jj_consume_token(ELEMENT_NAME);
      break;
    case IDENT:
      jj_consume_token(IDENT);
      break;
    default:
      jj_la1[11] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public void solitary_pseudo_class() throws ParseException {
    jj_consume_token(SOLITARY_PSEUDO_CLASS);
  }

  final public void solitary_class() throws ParseException {
    jj_consume_token(SOLITARY_CLASS);
  }

  final public void pseudo_element() throws ParseException {
    jj_consume_token(PSEUDO_ELEMENT);
  }

  final public void solitary_pseudo_element() throws ParseException {
    jj_consume_token(SOLITARY_PSEUDO_ELEMENT);
  }

  final public void solitary_id() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case SOLITARY_ID:
      jj_consume_token(SOLITARY_ID);
      break;
    case HASH:
      jj_consume_token(HASH);
      break;
    case HEX_COLOR:
      jj_consume_token(HEX_COLOR);
      break;
    default:
      jj_la1[12] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public Property declaration() throws ParseException {
  String name; String value; boolean isImportant = false;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case IDENT:
      name = property();
      jj_consume_token(COLON);
      value = expr();
      switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
      case IMPORTANT_SYM:
        prio();
                                                       isImportant = true;
        break;
      default:
        jj_la1[13] = jj_gen;
        ;
      }
                                                                               {if (true) return new Property(name, value, isImportant);}
      break;
    default:
      jj_la1[14] = jj_gen;
      ;
    }
      {if (true) return null;}
    throw new Error(Messages.getString("swing.err.15")); //$NON-NLS-1$
  }

  final public Property declaration_recoverable() throws ParseException {
  Property result = null;
    try {
      result = declaration();
    } catch (ParseException e) {
      skipTillDeclarationEnd();
    }
      {if (true) return result;}
    throw new Error(Messages.getString("swing.err.15")); //$NON-NLS-1$
  }

  final public void prio() throws ParseException {
    jj_consume_token(IMPORTANT_SYM);
  }

  final public String expr() throws ParseException {
  StringBuffer result = new StringBuffer(); String term; String oper; boolean operSet;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case PLUS:
    case MINUS:
    case EMS:
    case EXS:
    case LENGTH:
    case PERCENTAGE:
    case NUMBER:
    case RGB:
    case URL:
    case HEX_COLOR:
    case IDENT:
    case STRING:
      term = term();
                    result.append(term);
      label_7:
      while (true) {
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case COMMA:
        case SLASH:
        case PLUS:
        case MINUS:
        case EMS:
        case EXS:
        case LENGTH:
        case PERCENTAGE:
        case NUMBER:
        case RGB:
        case URL:
        case HEX_COLOR:
        case IDENT:
        case STRING:
          ;
          break;
        default:
          jj_la1[15] = jj_gen;
          break label_7;
        }
                                            operSet = false;
        switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
        case COMMA:
        case SLASH:
          oper = oper();
                                                                              result.append(oper); operSet = true;
          break;
        default:
          jj_la1[16] = jj_gen;
          ;
        }
        term = term();
                                                                                                                                    if (!operSet) {result.append(" ");} result.append(term);
      }
      break;
    default:
      jj_la1[17] = jj_gen;
      ;
    }
      {if (true) return result.toString();}
    throw new Error(Messages.getString("swing.err.15")); //$NON-NLS-1$
  }

  final public String term() throws ParseException {
  Token startToken = getToken(1);
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case PLUS:
    case MINUS:
      unary_oper();
      break;
    default:
      jj_la1[18] = jj_gen;
      ;
    }
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case NUMBER:
      jj_consume_token(NUMBER);
      break;
    case STRING:
      jj_consume_token(STRING);
      break;
    case PERCENTAGE:
      jj_consume_token(PERCENTAGE);
      break;
    case LENGTH:
      jj_consume_token(LENGTH);
      break;
    case EMS:
      jj_consume_token(EMS);
      break;
    case EXS:
      jj_consume_token(EXS);
      break;
    case IDENT:
      jj_consume_token(IDENT);
      break;
    case HEX_COLOR:
      jj_consume_token(HEX_COLOR);
      break;
    case URL:
      jj_consume_token(URL);
      break;
    case RGB:
      jj_consume_token(RGB);
      break;
    default:
      jj_la1[19] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return TokenResolver.resolve(startToken, getToken(0), false);}
    throw new Error(Messages.getString("swing.err.15")); //$NON-NLS-1$
  }

  final public String oper() throws ParseException {
  Token oper;
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case SLASH:
      oper = jj_consume_token(SLASH);
      break;
    case COMMA:
      oper = jj_consume_token(COMMA);
      break;
    default:
      jj_la1[20] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
      {if (true) return TokenResolver.resolve(oper);}
    throw new Error(Messages.getString("swing.err.15")); //$NON-NLS-1$
  }

  final public void unary_oper() throws ParseException {
    switch ((jj_ntk==-1)?jj_ntk():jj_ntk) {
    case MINUS:
      jj_consume_token(MINUS);
      break;
    case PLUS:
      jj_consume_token(PLUS);
      break;
    default:
      jj_la1[21] = jj_gen;
      jj_consume_token(-1);
      throw new ParseException();
    }
  }

  final public String property() throws ParseException {
  Token name;
    name = jj_consume_token(IDENT);
      {if (true) return TokenResolver.resolve(name);}
    throw new Error(Messages.getString("swing.err.15")); //$NON-NLS-1$
  }

  void skipTillExpressionEnd() throws ParseException {
    skipTillRuleSetEnd();
  }

  public CSSParserTokenManager token_source;
  SimpleCharStream jj_input_stream;
  public Token token, jj_nt;
  private int jj_ntk;
  private int jj_gen;
  final private int[] jj_la1 = new int[22];
  static private int[] jj_la1_0;
  static private int[] jj_la1_1;
  static {
      jj_la1_0();
      jj_la1_1();
   }
   private static void jj_la1_0() {
      jj_la1_0 = new int[] {0x400000,0x40000000,0x80000000,0x80000000,0x1000,0x400,0x80000000,0x0,0x0,0x80000000,0x80000000,0x0,0x80000000,0x0,0x0,0xff381000,0x81000,0xff300000,0x300000,0xff000000,0x81000,0x300000,};
   }
   private static void jj_la1_1() {
      jj_la1_1 = new int[] {0x0,0x200,0xdf,0xdf,0x0,0x0,0x9f,0x60,0x60,0xdf,0x9f,0x3,0x84,0x400,0x1,0x201,0x0,0x201,0x0,0x201,0x0,0x0,};
   }

  public CSSParser(java.io.InputStream stream) {
     this(stream, null);
  }
  public CSSParser(java.io.InputStream stream, String encoding) {
    try { jj_input_stream = new SimpleCharStream(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source = new CSSParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 22; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.InputStream stream) {
     ReInit(stream, null);
  }
  public void ReInit(java.io.InputStream stream, String encoding) {
    try { jj_input_stream.ReInit(stream, encoding, 1, 1); } catch(java.io.UnsupportedEncodingException e) { throw new RuntimeException(e); }
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 22; i++) jj_la1[i] = -1;
  }

  public CSSParser(java.io.Reader stream) {
    jj_input_stream = new SimpleCharStream(stream, 1, 1);
    token_source = new CSSParserTokenManager(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 22; i++) jj_la1[i] = -1;
  }

  public void ReInit(java.io.Reader stream) {
    jj_input_stream.ReInit(stream, 1, 1);
    token_source.ReInit(jj_input_stream);
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 22; i++) jj_la1[i] = -1;
  }

  public CSSParser(CSSParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 22; i++) jj_la1[i] = -1;
  }

  public void ReInit(CSSParserTokenManager tm) {
    token_source = tm;
    token = new Token();
    jj_ntk = -1;
    jj_gen = 0;
    for (int i = 0; i < 22; i++) jj_la1[i] = -1;
  }

  final private Token jj_consume_token(int kind) throws ParseException {
    Token oldToken;
    if ((oldToken = token).next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    if (token.kind == kind) {
      jj_gen++;
      return token;
    }
    token = oldToken;
    jj_kind = kind;
    throw generateParseException();
  }

  final public Token getNextToken() {
    if (token.next != null) token = token.next;
    else token = token.next = token_source.getNextToken();
    jj_ntk = -1;
    jj_gen++;
    return token;
  }

  final public Token getToken(int index) {
    Token t = token;
    for (int i = 0; i < index; i++) {
      if (t.next != null) t = t.next;
      else t = t.next = token_source.getNextToken();
    }
    return t;
  }

  final private int jj_ntk() {
    if ((jj_nt=token.next) == null)
      return (jj_ntk = (token.next=token_source.getNextToken()).kind);
    else
      return (jj_ntk = jj_nt.kind);
  }

  private java.util.Vector jj_expentries = new java.util.Vector();
  private int[] jj_expentry;
  private int jj_kind = -1;

  public ParseException generateParseException() {
    jj_expentries.removeAllElements();
    boolean[] la1tokens = new boolean[54];
    for (int i = 0; i < 54; i++) {
      la1tokens[i] = false;
    }
    if (jj_kind >= 0) {
      la1tokens[jj_kind] = true;
      jj_kind = -1;
    }
    for (int i = 0; i < 22; i++) {
      if (jj_la1[i] == jj_gen) {
        for (int j = 0; j < 32; j++) {
          if ((jj_la1_0[i] & (1<<j)) != 0) {
            la1tokens[j] = true;
          }
          if ((jj_la1_1[i] & (1<<j)) != 0) {
            la1tokens[32+j] = true;
          }
        }
      }
    }
    for (int i = 0; i < 54; i++) {
      if (la1tokens[i]) {
        jj_expentry = new int[1];
        jj_expentry[0] = i;
        jj_expentries.addElement(jj_expentry);
      }
    }
    int[][] exptokseq = new int[jj_expentries.size()][];
    for (int i = 0; i < jj_expentries.size(); i++) {
      exptokseq[i] = (int[])jj_expentries.elementAt(i);
    }
    return new ParseException(token, exptokseq, tokenImage);
  }

  final public void enable_tracing() {
  }

  final public void disable_tracing() {
  }

/*
    private static String getTokenInfo(final Token t) {
         return t.image + " at " + t.beginLine + "x" + t.beginColumn;
    }
    */
}
