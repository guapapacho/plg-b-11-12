PROGRAM ejer4blab2(INPUT,OUTPUT);
USES crt;
VAR
 mes,anyo,ndias:INTEGER;
 bisiesto:BOOLEAN;
BEGIN
 clrscr;
 WRITE('Introduzca un a¤o: ');
 READLN(anyo);
 WRITE('Introduzca un mes: ');
 READLN(mes);
 IF(anyo<1)OR(mes<1)OR(mes>12)THEN WRITELN('La fecha no es v lida')
 ELSE
  BEGIN
   bisiesto:=(anyo MOD 4=0)AND NOT((anyo MOD 100=0)AND(anyo MOD 400<>0));
   CASE mes OF
    1,3,5,7,8,10,12: ndias:=31;
    4,6,9,11:        ndias:=30;
    2:               IF bisiesto THEN ndias:=29
                     ELSE ndias:=28;
   END;
   WRITELN('El mes ',mes,' del a¤o ',anyo,' tiene ',ndias,' d¡as');
  END;
 READKEY;
END.
