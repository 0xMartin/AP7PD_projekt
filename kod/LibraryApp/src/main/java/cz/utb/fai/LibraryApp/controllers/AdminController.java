package cz.utb.fai.LibraryApp.controllers;

import cz.utb.fai.LibraryApp.AppRequestMapping;
import cz.utb.fai.LibraryApp.bussines.enums.EProfileState;
import cz.utb.fai.LibraryApp.bussines.services.BookService;
import cz.utb.fai.LibraryApp.bussines.services.BorrowService;
import cz.utb.fai.LibraryApp.bussines.services.UserService;
import cz.utb.fai.LibraryApp.model.Book;
import cz.utb.fai.LibraryApp.model.User;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping(value = AppRequestMapping.ADMIN)
public class AdminController {

  @Autowired
  protected UserService userService;

  @Autowired
  protected BookService bookService;

  @Autowired
  protected BorrowService borrowService;

  // ###########################################################################################################
  // DEFAULT
  // ###########################################################################################################

  /**
   * Zakladni view (notifikace a jejich potvrzovani, vypujcene knihy, odkazy na
   * spravu uzivatelu a knih)
   * 
   * @param username  Uzivatelske jmeno uzivatele jehoz ucet bude
   *                  potvrzen/zamitnut
   * @param confirmed True:Potvrzen/False:Zamitrnut
   * @return Nazev view
   */
  @GetMapping
  public String home(
      Model model,
      @RequestParam(required = false) String username,
      @RequestParam(required = false) Boolean confirmed) {
    try {
      // pokud je specifikovano uzivatelske jmeno a stav potvrzeni => provede se zmena
      // stavu uctu
      if (username != null && confirmed != null) {
        this.userService.setProfileState(
            username,
            confirmed ? EProfileState.CONFIRMED : EProfileState.NOT_CONFIRMED);
      }
      // notifikace -> uzivatele, kteri cekaji na schvaleni
      List<User> users = userService.findUsersWithWaitingState();
      model.addAttribute("NOTIFICATIONS", users);
    } catch (Exception e) {
      model.addAttribute(AppRequestMapping.RESPONSE_ERROR, e.getMessage());
    }
    return AppRequestMapping.VIEW_PREFIX + "/admin/admin";
  }

  // ###########################################################################################################
  // SPRAVA UZIVATELU
  // ###########################################################################################################

  /**
   * Navrati view se spravou pro uzivatele
   * 
   * @param model ViewModel
   * @return Nazev view
   */
  @GetMapping("/users")
  public String users(Model model) {
    List<User> users = this.userService.users();
    model.addAttribute("USERS", users);
    return AppRequestMapping.VIEW_PREFIX + "/admin/users";
  }

  /**
   * Zobrazi view pro uzivatele a zaroven odstrani specifikovaneho uzivatele
   * 
   * @param model    ViewModel
   * @param username Uzivatelske jmeno uzivatele, ktery bude odstranen
   * @return Nazev view
   */
  @GetMapping("/userDelete")
  public String userDelete(Model model, @RequestParam String username) {
    try {
      this.userService.removeUser(username);
      model.addAttribute(
          AppRequestMapping.RESPONSE_SUCCESS,
          String.format("User '%s' successfully removed", username));
    } catch (Exception e) {
      List<User> users = this.userService.users();
      model.addAttribute("USERS", users);
      model.addAttribute(AppRequestMapping.RESPONSE_ERROR, e.getMessage());
    }

    List<User> users = this.userService.users();
    model.addAttribute("USERS", users);

    return AppRequestMapping.VIEW_PREFIX + "/admin/users";
  }

  /**
   * Zobrazi view pro uzivatele a zaroven zabanuje/odbanuje specifikovaneho
   * uzivatele
   * 
   * @param model    ViewModel
   * @param username Uzivatelske jmeno uzivatele, ktery bude zabanovan
   * @param ban      True -> zabanuje, False -> odbanuje
   * @return Nazev view
   */
  @GetMapping("/setUserState")
  public String setUserState(
      Model model,
      @RequestParam String username,
      @RequestParam EProfileState state) {
    try {
      this.userService.setProfileState(username, state);
      model.addAttribute(
          AppRequestMapping.RESPONSE_SUCCESS,
          String.format(
              "User '%s' profile state successfully set on %s",
              username,
              state.toString()));
    } catch (Exception e) {
      model.addAttribute(AppRequestMapping.RESPONSE_ERROR, e.getMessage());
    }
    List<User> users = this.userService.users();
    model.addAttribute("USERS", users);

    return AppRequestMapping.VIEW_PREFIX + "/admin/users";
  }

  /**
   * Navrati view pro vytvoreni noveho uzivatle
   * 
   * @param model ViweModel
   * @return Nazev view
   */
  @GetMapping("createUser")
  public String createUser(Model model) {
    return AppRequestMapping.VIEW_PREFIX + "/admin/create_user";
  }

  /**
   * Navrti view pro vytvoreni noveho uzivatele a vytvari noveho uzivatele dle
   * predanych paramteru
   * 
   * @param model ViewModel
   * @param user  Novy uzivatel
   * @return Nazev view
   */
  @PostMapping(path = "createUser", consumes = "application/x-www-form-urlencoded")
  public String createUser(Model model, User user) {
    try {
      // vytvori a automaticky potvrdi uzivatele
      this.userService.createUser(user);
      this.userService.setProfileState(
          user.getUsername(),
          EProfileState.CONFIRMED);
      model.addAttribute(
          AppRequestMapping.RESPONSE_SUCCESS,
          String.format("%s created successfully", user.getUsername()));
    } catch (Exception e) {
      model.addAttribute(AppRequestMapping.RESPONSE_ERROR, e.getMessage());
    }
    return AppRequestMapping.VIEW_PREFIX + "/admin/create_user";
  }

  /**
   * Navrati view pro editaci uzivatele
   * 
   * @param model    ViewModel
   * @param username Uzivatelske jmeno uzivatel, ktery bude editovan
   * @return Navez view
   */
  @GetMapping("editUser")
  public String editUser(Model model, @RequestParam String username) {
    try {
      User u = this.userService.findUser(username);
      model.addAttribute("USER", u);
    } catch (Exception e) {
      model.addAttribute(AppRequestMapping.RESPONSE_ERROR, e.getMessage());
    }
    return AppRequestMapping.VIEW_PREFIX + "/admin/edit_user";
  }

  /**
   * Navrati view pro editaci uzivatele a provede zmeny uzivatele
   * 
   * @param model    ViewModel
   * @param username Uzivatelske jmeno uzivatele jehoz parametry budou pozmeneny
   * @param user     Nove paramtery uzivatele
   * @return Nazev View
   */
  @PostMapping(path = "editUser", consumes = "application/x-www-form-urlencoded")
  public String editUser(
      Model model,
      @RequestParam String username,
      User user,
      EProfileState userState) {
    try {
      User n = this.userService.editUser(username, user);
      this.userService.setProfileState(username, EProfileState.CONFIRMED);
      this.userService.setProfileState(username, userState);
      model.addAttribute("USER", n);

      model.addAttribute(
          AppRequestMapping.RESPONSE_SUCCESS,
          "Changed successfully");
    } catch (Exception e) {
      model.addAttribute(AppRequestMapping.RESPONSE_ERROR, e.getMessage());
    }
    return AppRequestMapping.VIEW_PREFIX + "/admin/edit_user";
  }

  // ###########################################################################################################
  // SPRAVA KNIH
  // ###########################################################################################################

  /**
   * Navrati view se spravou pro knihy
   * 
   * @param model
   * @return Navez view
   */
  @GetMapping("/books")
  public String books(Model model) {
    model.addAttribute("BOOKS", this.bookService.books());
    return AppRequestMapping.VIEW_PREFIX + "/admin/books";
  }

  /**
   * Navrati view pro vytvoreni noveh knihy
   * 
   * @param model ViewModel
   * @return Navez view
   */
  @GetMapping("/createBook")
  public String createBook(Model model) {
    return AppRequestMapping.VIEW_PREFIX + "/admin/create_book";
  }

  /**
   * Navrati view pro vytvoreni noveh knihy a zaroven vytvori knihu dle predanych
   * dat
   * 
   * @param model ViewModel
   * @param book  Parametry nove knihy
   * @param image Obrazek nove knihy
   * @return Navez view
   */
  @PostMapping(path = "/createBook", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public String createBook(
      Model model,
      @RequestParam("bookImage") MultipartFile bookImage,
      @RequestParam(name = "name") String name,
      @RequestParam(name = "author") String author,
      @RequestParam(name = "pageCount") Long pageCount,
      @RequestParam(name = "yearOfPublication") Long yearOfPublication,
      @RequestParam(name = "available") Long available) {
    try {
      Book book = new Book(
          0L,
          name,
          author,
          pageCount,
          yearOfPublication,
          "",
          available,
          0L);
      this.bookService.createBook(book, bookImage);

      model.addAttribute(
          AppRequestMapping.RESPONSE_SUCCESS,
          "Book successfully created");
    } catch (Exception e) {
      model.addAttribute(AppRequestMapping.RESPONSE_ERROR, e.getMessage());
    }
    return AppRequestMapping.VIEW_PREFIX + "/admin/create_book";
  }

  /**
   * Navrati view pro editaci knihy
   * 
   * @param model ViewModel
   * @param id    ID knihy
   * @return Navez view
   */
  @GetMapping("editBook")
  public String editBook(Model model, @RequestParam Long id) {
    try {
      Book b = this.bookService.findBook(id);
      model.addAttribute("BOOK", b);
    } catch (Exception e) {
      model.addAttribute(AppRequestMapping.RESPONSE_ERROR, e.getMessage());
    }
    return AppRequestMapping.VIEW_PREFIX + "/admin/edit_book";
  }

  /**
   * Navrati view pro editaci uzivatele a provede zmeny knihy
   * 
   * @param model ViewModel
   * @param id    ID knihy jejiz parametry budou pozmeneny
   * @param book  Nove paramtery knihy
   * @return Nazev View
   */
  @PostMapping(path = "editBook", consumes = "application/x-www-form-urlencoded")
  public String editBook(
      Model model,
      @RequestParam Long id,
      Book book) {
    try {
      Book b = this.bookService.updateBook(id, book);
      model.addAttribute("BOOK", b);

      model.addAttribute(
          AppRequestMapping.RESPONSE_SUCCESS,
          "Changed successfully");
    } catch (Exception e) {
      model.addAttribute(AppRequestMapping.RESPONSE_ERROR, e.getMessage());
    }
    return AppRequestMapping.VIEW_PREFIX + "/admin/edit_book";
  }

  /**
   * Zobrazi view pro knihy a zaroven odstrani specifikovannou knihu
   * 
   * @param model ViewModel
   * @param id    ID knihy, ktera bude odstranena
   * @return Nazev view
   */
  @GetMapping("/bookDelete")
  public String bookDelete(Model model, @RequestParam Long id) {
    try {
      this.bookService.removeBook(id);
      model.addAttribute(
          AppRequestMapping.RESPONSE_SUCCESS,
          String.format("User '%s' successfully removed", id));
    } catch (Exception e) {
      List<Book> books = this.bookService.books();
      model.addAttribute("BOOKS", books);
      model.addAttribute(AppRequestMapping.RESPONSE_ERROR, e.getMessage());
    }

    List<Book> books = this.bookService.books();
    model.addAttribute("BOOKS", books);

    return AppRequestMapping.VIEW_PREFIX + "/admin/books";
  }

}
