package logic;
import model.Polinomio;
import java.util.ArrayList;
import logic.OperacionesPolinomios.ResultadoDivisionLineal;

public class AnalisisPolinomio {
	//ATRIBUTOS
	private Polinomio polinomio;//ultimo polinomio que se analizó (null si todavía no se analizó ninguno)
	private ArrayList<Integer> enteras;//raíces enteras de ese polinomio
	private ArrayList<Double> racionales;//raíces racionales (no enteras) de ese polinomio
	
	//MÉTODOS
	//-----------------Raices Enteras------------------
	public ArrayList<Integer> raicesEnteras(Polinomio p){
		ArrayList<Integer> raices = new ArrayList<>();
		if(p.getGrado()!=0){
			if(!todosEnteros(p)){//Caso donde veo si todos los coeficientes NO son enteros
				Polinomio q=volverEnteros(p);//Vuelvo a hacer el polinomio con todos los coeficientes enteros
				if(q!=p){
					System.out.println("Se ha modificado el polinomio para que todos sus coeficientes sean enteros: " + q);
					probarDivisores(q,raices);
				}
				else{
					System.out.println("No se pudo volver a armar el polinomio con coeficientes enteros");
				}
			}
			else{
				probarDivisores(p,raices);
			}
		}
		else{
			System.out.println("El polinomio no tiene grado, por lo que no tengo raíces");
		}
		return raices;
	}
	//-----------------Raices Racionales------------------
	public ArrayList<Double> raicesRacionales(Polinomio p){
		ArrayList<Double> raices= new ArrayList<>();
		if(p.getGrado()!=0){
			if(!todosEnteros(p)){
				Polinomio q=volverEnteros(p);
				if(q==p){
				 System.out.println("No se pudo pasar a coeficientes enteros, no se analizan raíces racionales");
				}
				else{
					System.out.println("Polinomio con coeficientes enteros: " + q);
	                probarFracciones(q, raices);
				}
			}
			else{
				double a_n=coeficientePrincipal(p);
				if(a_n!=1 && a_n!=-1){
					probarFracciones(p,raices);
				}
				else{
					System.out.println("El polinomio tiene coeficiente principal 1,tenemos un polinomio mónico, por lo que sus raíces racionales son enteras");
				}
			}
		}
		else{
			System.out.println("El polinomio no tiene grado, por lo que no tengo raíces");
		}
		return raices;
	}
	
	/*Este método pasa un nro decimal a un nro fracción para poder mostrarlo, busca el menor denominador "den" que al multiplicarlo por el nro
	da un entero (es la misma idea que se usa en volverEnteros, metodo que lo vemos mas abajo)*/
	public String aFraccion(double x){
		for(int den=1;den<=1000;den++){
			if(esEntero(x*den)){
				long num=Math.round(x*den);//se redondea para limpiar -1.9999999999 -> -2
				if(den==1){
					return String.valueOf(num);//era un entero, no hace falta mostrar "/1"
				}
				return num+"/"+den;
			}
		}
		return String.valueOf(x);//si no se encontró una fracción, se muestra como decimal
	}


	//------------------------MÉTODOS AUXILIARES------------------------

	/*Lo que se hace este método es ver si es que el coeficientes es entero o no, 
	por lo que si el coeficiente es entero cumplirá con la tolerancia de ser menor que 1*10^(-10) 
	y devolverá "true" en caso de serlo y "false" en caso de no serlo */
	private boolean esEntero(double nro) {
		if (Math.abs(nro - Math.round(nro)) < 1e-10) {
			return true;
		} 
		else {
			return false;
		}
	}
	//Si todos los coeficientes son enteros da "true" sino "false"
	private boolean todosEnteros(Polinomio p){
		for(int i=0;i<=p.getGrado();i++){
			if(!esEntero(p.getCoeficiente(i))){
				return false;
			}
		}
		return true;
	}
	/*Este método lo que hace es volver enteros a los coeficientes fraccionarios, lo hace con un valor "k"
	que al multiplicarlo por la fraccion me da un nro entero, pero multiplicar todo el polinomio por ese "k"
	y me devuelve el polinomio modificado pero con todos los coefiecientes enteros*/
	private Polinomio volverEnteros (Polinomio p){
		double [] c=p.getCoeficientes();//copia los coeficientes del polinomio en c
		Polinomio q=p;
		for(int i=2;i<1000;i++){//se comienza probando con 2 como mcm, sino va aumentando
			double[] lista=new double[c.length];//se crea un nuevo vector vacio de coeficientes con el mismo tamaño que el polinomio original
			boolean nros=true; //funciona para ver si es que con ese valor de "i" queda el nro entero
			for(int j=0;j<c.length;j++){
				double x=c[j]*i;//se multiplica cada coeficiente por el mcm y se lo guarda en el nuevo vector
				if(!esEntero(x)){
					nros=false;
				}
				else{
					lista[j]=Math.round(x);
				}
			}
			if(nros){
				q = new Polinomio(lista);
				break;//ya se encontro el mcm, entonces salgo del ciclo for
			}
		}
		return q;
	}
	//Este método es el que se fija si es que el polinomio tiene raíces enteras, y si es así las devuelve en un ArrayList
	private void probarDivisores(Polinomio p, ArrayList<Integer> raices){
		if(Math.round(terminoIndependiente(p)) == 0){//en caso del que termino independiente sea cero
			raices.add(0);//x=0 es una raiz por que anula toda la expresion
			p=quitarCeros(p);//el polinomio se actualizo y sigue con lo que queda despues de sacar el factor x
		}
		if(p.getGrado()!=0){
			double d=terminoIndependiente(p);//Obtengo el termino independiente del polinomio
			ArrayList<Integer>divisores=obtenerDivisores(d);//Obtengo los divisores del termino independiente
			for(int i=0;i<divisores.size();i++){
				int r=divisores.get(i);
				ResultadoDivisionLineal resultado = OperacionesPolinomios.dividirHorner(p, -r);
				if(Math.abs(resultado.resto)<1e-10){
					raices.add(r);
				}
			}
		}
		
	}
	//Método para obtener los divisores de un nro
	private ArrayList<Integer> obtenerDivisores(double numero) {
		ArrayList<Integer> divisores = new ArrayList<>();
		int nro = (int) Math.abs(Math.round(numero));//trabajamos con el valor absoluto del termino independiente
		for (int i = 1; i <= nro; i++) {
			if (nro % i == 0) {//si el indice divide exacto al nro
				divisores.add( i);//agreaga al Array el nro +
				divisores.add (-i);//agreaga al Array el nro -
			}
		}
		return divisores;
	}

	
	/*Lo que hace este método es devolver el termino independiente del polinomio, es decir el a_0
	Podemos ver que el tamaño del polinomio es "n" entonces en el indice en la posicion
	"n" estara el termino independiente*/
	private double terminoIndependiente(Polinomio p){
		int n = p.getGrado();
		double coeficiente = p.getCoeficiente(n);
		return coeficiente;
	}

	//Busco el valor del coeficiente principal
	private double coeficientePrincipal(Polinomio p){
		double a_n=p.getCoeficiente(0);
		return a_n;
	}


	//Buscamos las combinaciones posibles entre a_0/a_n que no son enteros y no se repiten
	private ArrayList<Double> combinaciones(Polinomio polinomio){
		double q=coeficientePrincipal(polinomio);
		double p=terminoIndependiente(polinomio);
		ArrayList<Integer> divisoresQ=obtenerDivisores(q);
		ArrayList<Integer> divisoresP=obtenerDivisores(p);
		ArrayList<Double> combinaciones = new ArrayList<>();
		for(int i=0;i<divisoresQ.size();i++){
			for(int j=0;j<divisoresP.size();j++){
				double x= (double) divisoresP.get(j)/divisoresQ.get(i);
				if(!esEntero(x)){
					int b=0;
					for (int t=0;t<combinaciones.size();t++){
						if (Math.abs(combinaciones.get(t) - x) < 1e-10) {//me fijo si en la lista esta el mismo nro 
							b=1;
							break;
						}
                    }
					if(b==0){
						combinaciones.add(x);
					}
				}
			}
		}
		return combinaciones;
	}
	//Este método es el que se fija si es que el polinomio tiene raíces racionales, y si es así las devuelve en un ArrayList
	private void probarFracciones(Polinomio p, ArrayList<Double> raices){
		p=quitarCeros(p);
		if(p.getGrado()!=0){
			ArrayList<Double> divisores=combinaciones(p);
			for(int i=0;i<divisores.size();i++){
				double r=divisores.get(i);
				ResultadoDivisionLineal resultado = OperacionesPolinomios.dividirHorner(p, -r);
				if(Math.abs(resultado.resto)<1e-10){
					raices.add(r);
				}
			}
		}
	}

	//Saca factor comun "x" quitando los coeficientes 0 del final
	private Polinomio quitarCeros(Polinomio p){
		double [] c=p.getCoeficientes();
		int contador=0;
		while(contador<c.length-1 && c[c.length-1-contador]==0){
			contador++;
		}
		double [] lista=new double[c.length-contador];
		for(int i=0;i<lista.length;i++){
			lista[i]=c[i];
		}
		return new Polinomio(lista);
	}

	public void analizar(Polinomio p){
		polinomio=p;
		enteras= raicesEnteras(p);
		racionales= raicesRacionales(p);
	}

	//-------------------MUESTRA-------------------
	@Override
	public String toString(){
		String cadena=" ";
		if(polinomio==null){
			cadena="Todavia no se analizo ningun polinomio.";
		}
		else{
			String fracciones= "[";
			for(int i=0;i<racionales.size();i++){
				fracciones+=aFraccion(racionales.get(i));
				if(i<racionales.size()-1){
					fracciones+=", ";//coma entre las fracciones
				}
			}
			fracciones+="]";
			cadena= "Polinomio: "+polinomio+"\n"+ "Raíces enteras: "+enteras+"\n"+ "Raíces racionales: "+fracciones;
		}
		return cadena;
	}
}