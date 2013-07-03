package eu.compassresearch.core.typechecker;

import static eu.compassresearch.core.typechecker.TestUtil.addTestProgram;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

@RunWith(value = Parameterized.class)
public class CmlTypeCheckerTestCase extends AbstractTypeCheckerTestCase {

	@Parameters
	public static Collection<Object[]> parameter() {

		List<Object[]> testData = new LinkedList<Object[]>();
		// 0 // We have a challenge with class types in type-defs at top level
		addTestProgram(testData,
				"class T = begin end class N = begin end types TorN = T | N",
				true, false, new String[0]);
		// 1
		addTestProgram(
				testData,
				"process T = begin operations o:int*int ==> int o(a,b) == return a+b @ o(10,10) end",
				true, true, new String[0]);
		// 2
		addTestProgram(
				testData,
				"types I = int process T = begin operations o:I*I ==> int o(a,b) == return a+b @ o(10,10) end",
				true, true, new String[0]);
		// 3
		addTestProgram(testData, "types R :: a : int b : int " + "process P = "
				+ "  " + " " + "begin " + "  state " + "    a : int "
				+ "    k : R   " + "  actions "
				+ "    A = cases k : mk_R(a1,b1) -> a:=a1+b1 end" + " @ A "
				+ "end", true, true, new String[0]);
		// 4
		addTestProgram(
				testData,
				"types Value = int ID = nat process P = begin operations CheckMac: Value * Value * ID ==> bool CheckMac(a,b,c) == return (a*b=c) Me: () ==> bool Me() == (dcl a : Value := 2 @ return CheckMac(a,2,4)) @ Skip end ",
				true, true, new String[0]);
		// 5
		addTestProgram(
				testData,
				"channels out: nat1 process S1 = || k in set {1,2,3} @ [{ }] begin @ out!k -> Skip end",
				true, true, new String[0]);
		// 6
		addTestProgram(
				testData,
				"channels c1:int*int process A = begin state a:int @ [| {  } |] i in set {1,2,3} @ [{ c1 }] c1!i -> Skip end",
				true, false, new String[0]);
		// 7
		addTestProgram(
				testData,
				"channels c1:int*int process A = begin state a:int @ [| {a } |] i in set {1,2,3} @ [{    }] c1!i -> Skip end",
				true, false, new String[0]);
		// 8
		addTestProgram(
				testData,
				"channels c1:int*int process A = begin state a:int @ [| {c1} |] i in set {1,2,3} @ [{  a }] c1!i -> Skip end",
				true, true, new String[0]);
		// 9
		addTestProgram(
				testData,
				"channels c1:int*int process A = begin state a:int @ [| { } |] i in set {1,2,3} @ [{   }] Skip end",
				true, true, new String[0]);
		// 10
		addTestProgram(
				testData,
				"channels c1:int*int process A = begin state a:int @ [| {c1, a} |] i in set {1,2,3} @ [{   }] c1!i -> Skip end",
				true, false, new String[0]);
		// 11
		addTestProgram(
				testData,
				"channels c1:int*int process A = begin state a:int @ [| { } |] i in set {1,2,3} @ [{  c1, a }] c1!i -> Skip end",
				true, false, new String[0]);
		// 12
		addTestProgram(
				testData,
				"channels c1:int*int process A = begin state a:int @ [| { } |] i in set {1,2,3} @ [{  a  }] c1!a -> Skip end",
				true, false, new String[0]);
		// 13
		addTestProgram(
				testData,
				"channels c1:int*int process A = begin state a:int @ [| { c1 } |] i in set {1,2,3} @ [{   }] c1!a -> Skip end",
				true, false, new String[0]);
		// 14
		addTestProgram(
				testData,
				"process T = begin operations O: () ==> () O() == Skip @ O() end",
				true, true, new String[0]);
		// 15
		addTestProgram(testData,
				"process A = begin @ let b = 2 in [b = 2] & Skip end", true,
				true, new String[0]);
		// 16
		addTestProgram(
				testData,
				"values a : int = 42 process A = begin values b : int = 43 @Skip end",
				true, true, new String[0]);
		// 17
		addTestProgram(
				testData,
				"channels c process A = begin @ ||| k in set {1,2,3} @ [{ }]c!k -> Skip end",
				true, false, new String[0]);
		// 18
		addTestProgram(
				testData,
				"channels c:nat1 process A = begin @ ||| k in set {1,2,3} @ [{ }]c!k -> Skip end",
				true, true, new String[0]);
		// 19
		addTestProgram(testData,
				"process K = begin actions INIT = Skip @ INIT() end", true,
				true, new String[0]);
		// 20
		addTestProgram(
				testData,
				"process K = begin operations INIT: () ==> () INIT() == Skip @ INIT() end",
				true, true, new String[0]);
		// 21
		addTestProgram(
				testData,
				"values m: map int to int = {  1 |-> 2 } process K = begin state l:int functions f:map int to int -> int f(m) == m(42) @ l := f(m) end",
				true, true, new String[0]);
		// 22
		addTestProgram(
				testData,
				"values m = {  1 |-> 2 } process K = begin state l:int operations f:map int to int ==> int f(m) == return m(42) @ l := f(m) end",
				true, true, new String[0]);
		// 23
		addTestProgram(
				testData,
				"channels a: int types book = token values mbook = { mk_token(\"Book\") |-> 1} process P = begin @  a!(mbook(book))->Skip end",
				true, true, new String[0]);
		// 24
		addTestProgram(testData,
				"types k = int channels a class A = begin end", true, true,
				new String[0]);
		// 25
		addTestProgram(
				testData,
				"types S = seq of char RescueDetails::k : int functions rescueDetailsToString(r : RescueDetails) s: S post s <> [] process P = begin actions MERGE2 = val eru: ERUId @ (dcl s: S,r:RescueDetails @ s := rescueDetailsToString(r); Skip ) @ Skip end",
				true, true, new String[0]);
		// 26
		addTestProgram(
				testData,
				"channels c1: int process P = begin actions A = val r : int @ c1!r -> Skip @ Skip end",
				true, true, new String[0]);
		// 27
		addTestProgram(
				testData,
				"types Id ::   type : (<ERU> | <CC>) identifier : token ERUId = Id Location = token Criticality = nat inv c == c < 4 String = seq of char RescueDetails :: target : Location criticality : Criticality process CallCentreProc = begin state erus: set of ERUId eruRescues: map ERUId to RescueDetails inv dom eruRescues subset erus and (forall i in set erus @ i.type = <ERU>) operations reAllocateERU(eru : ERUId, r : RescueDetails) frame wr eruRescues : map ERUId to RescueDetails rd erus: set of ERUId pre eru in set erus and eru in set dom eruRescues and eruRescues(eru) <> r post eru in set dom eruRescues and eruRescues(eru) = r actions FORK1 = (dcl eru : ERUId @ (dcl r : RescueDetails @ (dcl oldr: RescueDetails @  reAllocateERU(eru,r)))) @ Skip end",
				true, true, new String[0]);
		// 28
		addTestProgram(
				testData,
				"types SUBS = token STATUS = ( <ringing> | <speech> | <suspended>) values Connected = {<ringing>,<speech>,<suspended>} functions connected: (map SUBS to STATUS) * (map SUBS to SUBS) +> (inmap SUBS to SUBS) connected(status,number) == {} free: (map SUBS to STATUS) * (map SUBS to SUBS) * (set of SUBS) +> (set of SUBS) free(status,number,subs) == subs \\ dom(status) \\ rng(connected(status,number)) class Exchange = begin end",
				true, true, new String[0]);
		// 29
		addTestProgram(
				testData,
				"channels a:int process P = ||| i in set {1,2,3} @ begin @ a!i -> Skip end",
				true, true, new String[0]);
		// 30
		addTestProgram(
				testData,
				"class T = begin state a : int functions f:int * int -> int f(x,y) == a + x + y end",
				true, false, new String[0]);
		// 220
		addTestProgram(
				testData,
				"types Quantity = int Price = int class C = begin state sellerBids : seq of Quantity buyerBids : seq of Quantity prices : seq of Price inv len(sellerBids) = len(buyerBids) and len(sellerBids) = len(prices) end",
				true, true, new String[0]);
		// 221
		addTestProgram(
				testData,
				"functions f: int -> int f(a) == a+1 pre a > 0 process P = begin @ f(2) end ",
				true, true, new String[0]);
		// 222
		addTestProgram(
				testData,
				"class C = begin operations public doit: int ==> () doit(a) == Skip end process P = begin state s : C @ s.doit(1) end",
				true, true, new String[0]);
		// 223
		addTestProgram(
				testData,
				"functions f: int -> int f(a) == a+1 pre a > 0 process P = begin @ pre_f(2) end ",
				true, true, new String[0]);
		// 224
		addTestProgram(
				testData,
				"types mac :: a:int b:int process P = begin functions f: mac * int -> int f(x,y) == x.a+y @ f(mk_mac(1,2),2) end",
				true, true, new String[0]);
		// 225
		addTestProgram(
				testData,
				"functions f: int * int -> int f(x,y) == x+y pre x > 0 process P = begin actions A = [ pre_f(0,0) ] & Skip @ A end",
				true, true, new String[0]);
		// 226
		addTestProgram(
				testData,
				"types ERUId = nat RescueDetails ::a:int b:int process P = begin state erus : set of ERUId eruRescues : map ERUId to RescueDetails operations findIdleERUs() idleERUs: set of ERUId frame rd erus: set of ERUId rd eruRescues: map ERUId to RescueDetails post idleERUs = erus \\ dom eruRescues @ findIdleERUs() end",
				true, true, new String[0]);
		// 227
		addTestProgram(
				testData,
				"channels c: nat values a : nat = 10 - 11 b:nat = 20 - 10 process A = begin actions B = c!(a-b)->Skip @ Skip end",
				true, true, new String[0]);
		// 228
		addTestProgram(
				testData,
				"process P = begin actions B = A1(1,2) A1 = val a:int, b: nat @ Skip  @ A1(1,1) end",
				true, true, new String[0]);
		// 229
		addTestProgram(
				testData,
				"types Day = nat AvailDB = map Day to nat functions CkAvail (d:Day,av:AvailDB) n:nat post n = av(d)",
				true, true, new String[0]);
		// 230
		addTestProgram(testData,
				"class C = begin operations C:()==>C C() == Skip end", true,
				true, new String[0]);
		// 231
		addTestProgram(
				testData,
				"class C = begin operations C:()==>C C() == Skip end process P = begin state c:C @ c := new C() end",
				true, true, new String[0]);
		// 232
		addTestProgram(
				testData,
				"process A = begin state i:int := 0 m:map int to (map int to int) @ m(0)(0) := 1 end",
				true, true, new String[0]);
		// 233
		addTestProgram(
				testData,
				"process P = begin state a: nat * nat := mk_(0,0) t: (nat * nat) * (nat * nat) @ t.#1 := a end",
				true, true, new String[0]);
		// 234 //test case for bug http://sourceforge.net/p/compassresearch/tickets/12/
		addTestProgram(testData, "channels a : int class A = begin state hello : int := 4 operations  A : () ==> A  A() == Skip getHello : () ==> int getHello() == return hello end process Test =  begin @ (dcl v : A, out : int @ v := new A(); out := v.getHello();a.(out) -> Skip) end", true, true, new String[0]);
		
		return testData;
	}

	public CmlTypeCheckerTestCase(String cmlSource, boolean parsesOk,
			boolean typesOk, String[] errorMessages) {
		super(cmlSource, parsesOk, typesOk, errorMessages);
	}

}
