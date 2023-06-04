package compiler;

import compiler.AST.*;
import compiler.lib.*;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class TypeRels {
	//il prima campo della map è l'id della classe, mentre il secondo è l'id della superclasse
	public static Map<String, String> superType = new HashMap<>();
	// valuta se il tipo "a" e' <= al tipo "b", dove "a" e "b" sono tipi di base: IntTypeNode o BoolTypeNode
	public static boolean isSubtype(TypeNode a, TypeNode b) {
		//valutiamo se RefTypeNode è un sottotipo. Per fare ciò utilizziamo superType
		if (a instanceof RefTypeNode && b instanceof RefTypeNode) {
			String directSuperType = ((RefTypeNode) a).id;
			//per capire se è un sottotipo dobbiamo fare la ricerca
			while (directSuperType != null && !directSuperType.equals(((RefTypeNode) b).id)) {
				directSuperType = superType.get(directSuperType);
			}
			return ((RefTypeNode) a).id.equals(((RefTypeNode) b).id) || directSuperType != null;
		}

		if (a instanceof ArrowTypeNode && b instanceof ArrowTypeNode) {
			return isSubtype(((ArrowTypeNode) a).ret, ((ArrowTypeNode) b).ret) && //valuto la relazione di co varianza sul ritorno
					IntStream.range(0, ((ArrowTypeNode) a).parlist.size())//valuto la contro varianza sul tipo dei parametri
							.allMatch(i -> isSubtype(
									((ArrowTypeNode) b).parlist.get(i),
									((ArrowTypeNode) a).parlist.get(i))
							);
		}
		return a.getClass().equals(b.getClass())
				|| ((a instanceof BoolTypeNode) && (b instanceof IntTypeNode))
				|| a instanceof EmptyTypeNode; //sottotipo di qualsiasi tipo
	}

	public static TypeNode lowestCommonAncestor(TypeNode a, TypeNode b) {
		//caso in cui trattiamo intTypeNode e BoolTypeNode
		if (isSubtype(a, new IntTypeNode()) && isSubtype(b, new IntTypeNode())){
			if (a instanceof IntTypeNode || b instanceof IntTypeNode){
				return new IntTypeNode();
			} else {
				return new BoolTypeNode();
			}
		}

		if ((a instanceof RefTypeNode || a instanceof EmptyTypeNode)
				&& (b instanceof RefTypeNode || b instanceof EmptyTypeNode)) {

			if (a instanceof EmptyTypeNode) return b;
			if (b instanceof EmptyTypeNode) return a;

			//all'inizio consideriamo la classe a. se questa non ha lo stesso tipo cerchiamo risalendo una classe che faccia in modo che b sia sottotipo
			if (((RefTypeNode) a).id.equals(((RefTypeNode) b).id)) {
				return a;
			}
			//con questo while cerchiamo un tipo che sia sottotipo sia di a che di b
			//quindi cerchiamo un tipo (una classe) da cui derivano le due classi
			String type = superType.get(((RefTypeNode) a).id);
			while(type != null && isSubtype(b, new RefTypeNode(type))) {
				type = superType.get(type);
			}
			return type != null ? new RefTypeNode(type) : null;
		}

		return null;
	}

}
