package logic;
import model.Polinomio;
import java.util.ArrayList;
import logic.OperacionesPolinomios.ResultadoDivisionLineal;

public class CotasDePolinomio{
    //-----------------METODOS DE COTAS------------------
    public double cotaLagrange(Polinomio p) {
        double cota;
        if(coeficientePrincipal(p)<0) {
           p=cambiarSigno(p);         
        }
        double a_0= coeficientePrincipal(p);
        int k=indiceDelPrimerCoeficienteNegativo(p);
        if(k!=-1){
           double A=coeficienteNegativoMaximo(p);
           cota=1+Math.pow(A/a_0,1.0/k);
           System.out.println("Se encontro la cota superior por Lagrange que es: " + cota);
           return cota;
        }
        else{
            System.out.println("No hay raíces positivas, por lo que no se puede calcular la cota de Lagrange");
            return 0;
        }
    }

    public double cotaLaguerre(Polinomio p) {
        if(p.getGrado()==0){//una constante no se puede dividir con Horner ni tiene raíces que acotar
            return 0;
        }
        double a_0= coeficientePrincipal(p);
        if(a_0<0) {
           p=cambiarSigno(p);         
        }
        double L=1;
        while(!esCotaLaguerre(p, L)){
            System.out.println("L = " + L + "no sirve, pruebo con el siguiente");
            L=L+0.5;
        }
        System.out.println("Se encontro la cota superior por Laguerre que es: " + L);
        return L;
    }

    public double cotaNewton(Polinomio p){
        if(p.getGrado()==0){//una constante no se puede dividir con Horner ni tiene raíces que acotar
            return 0;
        }
        double a_0= coeficientePrincipal(p);
        if(a_0<0){
            p=cambiarSigno(p);
        }
        double a=1;
        while(!esCotaNewton(p, a)){
            a=a+0.5;
        }
        System.out.println("Se encontro la cota superior por Newton que es: " + a);
        return a;
    }
    //-----------------CAMBIO DE VARIABLES------------------
    //Ponemos la opcion de cual metodo quiere usar en el main
    private double cotaSuperior(Polinomio p,int metodo){
        if(metodo==1){// metodo 1=Lagrange
            return cotaLagrange(p);
        }
        else if(metodo==2){//metodo 2=Laguerre
            return cotaLaguerre(p);
        }
        else if(metodo==3){//metodo 3=Newton
            return cotaNewton(p);
        }
        else{
            System.out.println("El metodo ingresado no es valido");
            return 0;
        }
    }
    //Este método solo me da la cota superior positiva usando cualquier metodo
    public double cotaSuperiorPositiva(Polinomio p,int metodo){
        if(p.getGrado()!=0){
            return cotaSuperior(p,metodo);
        }
        else{
            System.out.println("El polinomio no tiene grado, por lo que no tengo raíces");
            return 0;
        }
    }
    //Cota inferior de las raíces positivas: si la cota superior de x^n*P(1/x) es L, las raíces positivas de P son >= 1/L
    public double cotaInferiorPositiva(Polinomio p,int metodo){
        if(p.getGrado()!=0){
            double L=cotaSuperior(cambioCotaInferiorPositiva(p),metodo);
            if(L==0){//L=0 significa que no hay raíces positivas
                return 0;
            }
            return 1/L;
        }
        else{
            System.out.println("El polinomio no tiene grado, por lo que no tengo raíces");
            return 0;
        }
    }
    //Cota inferior de las raíces negativas: si la cota superior de P(-x) es L, las raíces negativas de P son >= -L
    public double cotaInferiorNegativa(Polinomio p,int metodo){
        if(p.getGrado()!=0){
            double L=cotaSuperior(cambioCotaInferiorNegativa(p),metodo);
            if(L==0){//L=0 significa que no hay raíces negativas
                return 0;
            }
            return -L;
        }
        else{
            System.out.println("El polinomio no tiene grado, por lo que no tengo raíces");
            return 0;
        }
    }

    //Cota superior de las raíces negativas: si la cota superior de x^n*P(-1/x) es L, las raíces negativas de P son <= -1/L
    public double cotaSuperiorNegativa(Polinomio p,int metodo){
        if(p.getGrado()!=0){
            double L=cotaSuperior(cambioCotaSuperiorNegativa(p),metodo);
            if(L==0){//L=0 significa que no hay raíces negativas
                return 0;
            }
            return -1/L;
        }
        else{
            System.out.println("El polinomio no tiene grado, por lo que no tengo raíces");
            return 0;
        }
    }

    //-----------------METODOS AUXILIARES------------------
    private double coeficientePrincipal(Polinomio p) {
        double a_0 = p.getCoeficiente(0);
        return a_0;
    }
    private double terminoIndependiente(Polinomio p) {
        double a_n = p.getCoeficiente(p.getGrado());
        return a_n;
    }
    private int indiceDelPrimerCoeficienteNegativo(Polinomio p) {
        for (int i = 0; i <= p.getGrado(); i++) {
            if (p.getCoeficiente(i) < 0) {
                return i;
            }
        }
        return -1; //Da -1 si no hay coeficientes negativos
    }
    private double coeficienteNegativoMaximo(Polinomio p) {
        double max=0;
        for (int i = 0; i <= p.getGrado(); i++) {
            double coef = p.getCoeficiente(i);
            if (coef < 0 && coef < max) {
                max = coef;
            }
        }
        max=Math.abs(max);
        return max;
    }
    //En caso del que el coeficiente principal sea negativo, se cambia el signo de todos los coeficientes del polinomio
    private Polinomio cambiarSigno(Polinomio p) {
        double[] c = p.getCoeficientes();
        for (int i = 0; i <= p.getGrado(); i++) {
            c[i] = -c[i];
        }
        return new Polinomio(c);
    }
    //Este método lo que hace es verificar si el valor ingresado es una cota de Laguerre, para eso aplica Horner con ese valor y verifica si todos los coeficientes del cociente resultante son positivos
    private boolean esCotaLaguerre(Polinomio p, double L){
        if(L<=0){//El L tiene que ser positivo
            return false;
        }
        else{
           ResultadoDivisionLineal resultado = OperacionesPolinomios.dividirHorner(p,-L);//Aplico Horner con ese L
           double[] coefs= resultado.cociente.getCoeficientes();
           for(int j=0; j<coefs.length; j++){
              if(coefs[j]<=0){
                return false;// hay algun coeficiente negativo o cero
              }
           }
           if(resultado.resto<=0){
              return false;// el resto es negativo o cero
           }
           else{
               return true;// todos los coeficientes del cociente y el resto son positivos
           }
        }  
    }
    
    //Este método lo que hace es verificar si el valor ingresado es una cota de Newton,
    //para eso aplica Horner con ese valor y verifica si todos los coeficientes del cociente resultante son positivos y de sus derivaddas tambien.
    private boolean esCotaNewton(Polinomio p, double a){
        if(a<=0){
            return false;
        }
        else{
            ArrayList<Polinomio> polinomios=sucesionDerivadas(p);
            for(int i=0; i<polinomios.size(); i++){
               Polinomio derivada=polinomios.get(i);
               if(derivada.getGrado()!=0){//si estamos ante una constante no se puede aplicar Ruffini por lo que esto se aplica para los casos donde la derivada es distinto de una constante
                   ResultadoDivisionLineal resultado = OperacionesPolinomios.dividirHorner(derivada, -a);
                   double [] coefs= resultado.cociente.getCoeficientes();
                   for(int j=0; j<coefs.length; j++){//miramos si todos los coeficientes del cociente son positivos, si hay alguno negativo o cero sale
                       if(coefs[j]<=0){
                            return false;
                       }
                    }
                   if(resultado.resto<=0){//miramos si el resto es positivo, si es negativo o cero sale
                        return false;
                   }
               }
            }
            return true;//da "true" cuando todos los coeficientes del polinomio y sus derivadasde son positivos
        }
    }
    //Hacemos la sucesion de derivadas de un polinomio
    private ArrayList<Polinomio> sucesionDerivadas(Polinomio p){
        ArrayList<Polinomio> derivadas = new ArrayList<>();
        derivadas.add(p); // Agregamos el polinomio original como la primera derivada (0ª derivada)
        for(int i=1; i<=p.getGrado(); i++){
            derivadas.add(derivadas.get(i-1).derivar());
        }
        return derivadas;
    }

    //--------METODOS CAMBIO DE VARIABLES-------------

    //1°Cambio de variable (cota inferior positiva x=1/t)
    private Polinomio cambioCotaInferiorPositiva(Polinomio p) {
        p=quitarCeros(p);//esto es para ver si es que el polinomio quedo en 0, si es asi saco el factor x, para que el nuevo polinomio no comience con 0
        double[] c = p.getCoeficientes();
        int n = c.length;
        double[] nuevos = new double[n];
        for (int i = 0; i < n; i++) {
            nuevos[i] = c[n - 1 - i];   // invierte el orden
        }
        return new Polinomio(nuevos);
    }

    //2° Cambio de variable (cota superior negatativa x=-1/t)
    private Polinomio cambioCotaSuperiorNegativa(Polinomio p){
        Polinomio q=cambioCotaInferiorPositiva(p);
        q=cambioCotaInferiorNegativa(q);
        return q;
    }
    //3° Cambio de variable (cota inferior negativa x=-t)
    private Polinomio cambioCotaInferiorNegativa(Polinomio p) {
        double[] c = p.getCoeficientes();
        int n = p.getGrado();
        double [] nuevo=new double[c.length];
        for (int i = 0; i <= n; i++) {
           nuevo[i]=c[i]* Math.pow(-1,n-i);//multiplica el coeficiente de la posicion par o impar y lo guarda en nuevo[i]
        }
        return new Polinomio(nuevo);//devuelve el polinomio modificado
    }

    //Para mostrar los nros mas prolijos los redondea a 4 decimales
    private double redondear(double x){
        return Math.round(x*10000)/10000.0;
    }
    //Saca factor común "x" quitando los coeficientes 0 del final
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

    //------------------------MUESTRA------------------------
    //Junta las cuatro cotas Y las muestra
    public String intervalos(Polinomio p,int metodo){
        if(metodo<1 || metodo>3){
            return "El méto ingresado no es correcto";
        }
        double supPositiva=cotaSuperiorPositiva(p,metodo);
        double infPositiva=cotaInferiorPositiva(p,metodo);
        double infNegativa=cotaInferiorNegativa(p,metodo);
        double supNegativa=cotaSuperiorNegativa(p,metodo);
       
        if(supPositiva==0){// no hay raíces positivas
            cadenaP="No hay raíces positivas";
        }
        else{
            cadenaP="Raíces positivas en [" + redondear(infPositiva) + ", " + redondear(supPositiva) + "]";
        }
        String cadenaN;

        if(infNegativa==0){//0 significa que no hay raíces negativas
            cadenaN="No hay raíces negativas";
        }
        else{
            cadenaN="Raíces negativas en [" + redondear(infNegativa) + ", " + redondear(supNegativa) + "]";
        }
        return cadenaP + "\n" + cadenaN;
    } 
}