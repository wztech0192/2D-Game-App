package game;

import object.Board;

public class GameSelector {
	
	public static Game selectGame(String type, Board b) {
		switch(type) {
		case "Dodge Ball":
			return new DodgeBall(b);
		case "Shooting":
			return new Shooting(b);
		case "Slash":
			return new Slash(b);
		case "Snake":
			return new Snake(b);
		case "Sword":
			return new Sword(b);
		case "Fractal Tree":
			return new FractalTree(b);
		case "Minecraft 2D":
			return new Minecraft2D(b);
		case "SPBLXS":
			return new SPBLXS(b);
		}
		return null;
	}
}
