package dominio;

import java.io.IOException;
import java.util.regex.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class ScrapperPC {

	public static void main(String[] args) throws IOException, InterruptedException {

		String producto = "portatil acer";

		String queryPc = "https://www.pccomponentes.com/buscar/?query=" + producto;
		Document docPc = Jsoup.connect(queryPc).get();

		Element link = docPc.select(".GTM-productClick").first();

		// Ya tenemos el primer resultado
		Document docElem = Jsoup.connect(link.absUrl("href")).get();

		Element nombre = docElem.select(".h4").first();
		System.out.println(nombre.text());

		Element precio = docElem.select("#precio-main").first();
		//Comprobamos que hemos recibido un precio correcto
		Pattern p = Pattern.compile("[0-9]*(.,[0-9]*)? [€$]");
		Matcher m = p.matcher(precio.text());
		if (m.matches()) {
			System.out.println("Precio: "+precio.text());
			String coste = precio.text();
			coste = coste.substring(0, coste.length() - 2);
			coste = coste.replace(",", ".");
			double costeDouble = Double.parseDouble(coste);
			//Return mensaje con el precio como double
		}

		Element ratingStars = docElem.select(".rating-stars").first();
		String rating = ratingStars.attr("style");
		//Comprobamos que coincide con el patron encontrado en la pagina web
		p = Pattern.compile("width: [0-9]*.[0-9]*%;");
		m = p.matcher(rating);
		if (m.matches()) {
			rating = rating.substring(7, rating.length() - 2);
			System.out.println("Rating: "+rating+" %");
			//Return Mensaje con el rating sin
		}
		
	}
}
