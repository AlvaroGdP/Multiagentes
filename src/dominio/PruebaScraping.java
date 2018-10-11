package dominio;

import java.util.*;
import java.io.IOException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class PruebaScraping {

	public static void main(String[] args) throws IOException, InterruptedException {
		
		String producto = "portatil acer";
		
		while (true) {
		
			System.out.println("-------------------------- PC Componentes ---------------------------\n");
			
			String queryPc = "https://www.pccomponentes.com/buscar/?query=" + producto;
			Document docPc = Jsoup.connect(queryPc).get();
			
			Elements linksPc = docPc.select("a[href]");
			List<String> urlsPc = new LinkedList<String>();
			
			System.out.println("Las urls de productos obtenidas con la query son: \n");
			
			//Iteramos sobre los enlaces obtenidos
			for (Element link: linksPc) {
				//Buscamos solo productos
				if (link.classNames().contains("GTM-productClick")){
					//Mostramos y almacenamos su url
					//Necesario if para evitar duplicados
					if (!urlsPc.contains(link.absUrl("href"))) {
						urlsPc.add(link.absUrl("href"));
						System.out.println(link.text()); //Tambien mostramos su nombre
						System.out.println(link.absUrl("href")+"\n");
					}
				}
			}
			
			System.out.println("---------------------------------------------");
			System.out.println("\nAhora obtendremos la información de cada producto.\n");
			
			//Ahora accedemos a cada url anterior
			for (String url: urlsPc) {
				Document doc_elem = Jsoup.connect(url).get();
				Elements nombre = doc_elem.select(".h4");
				Elements precio = doc_elem.select("#precio-main");
				// Y mostramos su precio y nombre
				System.out.println("El elemento "+ nombre.text() +"\ntiene un precio de: "+precio.text()+"\n");
			}
			
	
			System.out.println("\n\n-------------------------- AMAZON ---------------------------\n");
			
			String queryAmazon = "https://www.amazon.es/s/url=field-keywords=" + producto;
			Document docAmazon = Jsoup.connect(queryAmazon).get();
			
			Elements linksAmazon = docAmazon.select("a[href]");
			List<String> urlsAmazon = new LinkedList<String>();
			
			System.out.println("Las urls de productos obtenidas con la query son: \n");
			
			//Iteramos sobre los enlaces obtenidos
			for (Element link: linksAmazon) {
				//Buscamos solo productos
				if (link.classNames().contains("s-access-detail-page")) {
					//Mostramos y almacenamos su url, y mostramos tambien su nombre
					System.out.println(link.text());
					System.out.println(link.absUrl("href")+"\n");
					urlsAmazon.add(link.absUrl("href"));
				}
				
			}
	
			System.out.println("---------------------------------------------");
			System.out.println("\nAhora obtendremos la información de cada producto.\n");
			
			//Ahora accedemos a cada url anterior
			for (String url: urlsAmazon) {
				Document doc_elem = Jsoup.connect(url).get();
				Elements nombre = doc_elem.select("#productTitle");
				Elements precio1 = doc_elem.select("#priceblock_dealprice");
				Elements precio2 = doc_elem.select("#priceblock_ourprice");
				Elements precio3 = doc_elem.select(".a-size-mini");
				// Y mostramos el precio y nombre de cada producto
				// Necesario comprobar tres precios, Amazon no usa siempre el mismo
				if (!precio2.text().isEmpty() && precio2 != null) {
					System.out.println("El elemento "+ nombre.text() +"\ntiene un precio de: "+precio2.text()+"\n");
				}else {
					if (!precio1.text().isEmpty() && precio1 != null) {
						System.out.println("El elemento "+ nombre.text() +"\ntiene un precio de: "+precio1.text()+"\n");
					}else {
						//Si hemos llegado aqui, hay varias opciones con distinto precio para el producto
						System.out.println(obtenerPrecio(precio3, nombre));

					}
				}
			}
			Thread.sleep(10000);
		}
	}

	private static String obtenerPrecio(Elements precio3, Elements nombre) {
		for (int i=1; i<precio3.size(); i++) {
			if (!precio3.get(i).text().isEmpty() && precio3.get(i) != null) {
				return ("El elemento "+ nombre.text() +"\ntiene un precio de: "+precio3.get(i).text()+"\n");
			}
		}
		return "";
	}
	
}
