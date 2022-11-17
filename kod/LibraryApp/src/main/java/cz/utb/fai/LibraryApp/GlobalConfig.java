package cz.utb.fai.LibraryApp;

public class GlobalConfig {

  /**
   * Slozka, do ktere se budou uploadovat obrazky
   */
  public static final String UPLOAD_DIRECTORY = "images";

  /**
   * maximalni pocet knih, ktere si uzivatel muze vypujcit (zmena nebudou
   * provedeny na jiz vypujcene knihy)
   * [POCET KNIH]
   */
  public static long MAX_BORROWED_BOOKS = 6;

  /**
   * douba, na kterou si uzivatel pujcuje knihu (po uplynuti se kniha automaticky
   * vrati)
   * [DNY]
   */
  public static long BORROW_DAY_COUNT = 6;

  /**
   * minimalni delka hesla
   * [pocet znaku]
   */
  public static long MIN_PASSWORD_LENGTH = 8;
}
