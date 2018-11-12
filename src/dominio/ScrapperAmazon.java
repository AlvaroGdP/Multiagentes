package dominio;

import java.io.IOException;
import java.util.regex.*;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class ScrapperAmazon {

	public static void main(String[] args) throws IOException, InterruptedException {

		String producto = "portatil acer";

		String query = "https://www.amazon.es/s/url=field-keywords=" + producto;
		Document doc = Jsoup.connect(query).get();

		Element link = doc.select(".a-link-normal").first();

		// Ya tenemos el primer resultado
		Document docElem = Jsoup.connect(link.absUrl("href")).get();

		Element precio1 = docElem.select("#priceblock_dealprice").first();
		Element precio2 = docElem.select("#priceblock_ourprice").first();
		String precio;
		// Hay varios formatos de precio, debemos comprobarlos todos
		if (!precio2.text().isEmpty() && precio2 != null) {
			precio = precio2.text();
		}else {
			if (!precio1.text().isEmpty() && precio1 != null) {
				precio = precio1.text();
			}else {
				//Si hemos llegado aqui, hay varias opciones con distinto precio para el producto
				Elements precio3 = docElem.select(".a-size-mini");
				precio = obtenerPrecio(precio3);

			}
		}
		
		Pattern p = Pattern.compile("EUR [0-9]*(.,[0-9]*)?");
		Matcher m = p.matcher(precio);
		if (m.matches()) {
			System.out.println("Precio: " + precio);
			precio = precio.substring(4, precio.length());
			precio = precio.replace(",", ".");
			double precioDouble = Double.parseDouble(precio);
			// Return mensaje con el precio como double
		}
		
		Elements ratingStars = docElem.select(".a-declarative .a-icon-alt");
		//Ejemplo de formato: 4.2 de un máximo de 5 estrellas
		p = Pattern.compile("[0-9]([.,][0-9])? de[ a-zA-Zá0-9]*");
		m = p.matcher(ratingStars.text());
		if(m.matches()) {
			String[] valoracion = ratingStars.text().split("de"); //Obtenemos la primera parte de la cadena
			valoracion[0] = valoracion[0].replace(" ", "");
			System.out.println("Valoración: "+valoracion[0]);
			double valoracionDouble = Double.parseDouble(valoracion[0]);
			valoracionDouble = valoracionDouble/5; //Dividimos por el maximo
			valoracionDouble = valoracionDouble*100; //Obtenemos el valor porcentual
			//return mensaje con valoracion
		}

	}

	private static String obtenerPrecio(Elements precio3) {
		for (int i = 1; i < precio3.size(); i++) {
			if (!precio3.get(i).text().isEmpty() && precio3.get(i) != null) {
				return precio3.get(i).text();
			}
		}
		return "";
	}
}
