/**
 * Copyright (C) 2014 Benno Schön
 * 
 * This program is free software; you can redistribute it and/or modify it under the terms of the 
 * GNU General Public License as published by the Free Software Foundation; either version 2 of 
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; 
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU General Public License for more details. 
 * 
 * You should have received a copy of the GNU General Public License along with this program; 
 * if not, write to the Free Software Foundation, Inc., 51 Franklin St, Fifth Floor, 
 * Boston, MA 02110, USA 
 * 
 * @since 24.07.2014
 * @version 1.0
 * @author benno
 */
package mm.io.base;

import java.util.ArrayList;
import java.util.List;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class MemoryReservoir {

	private ReferenceQueue<Reservoire> queue = new ReferenceQueue<>();
	private List<SoftReference<Reservoire>> references = new ArrayList<>();
	
	public MemoryReservoir(int size){
		int reservoirSize = 1 << 16;
		int amount = 0;
		while (amount < size){
			references.add(new SoftReference<MemoryReservoir.Reservoire>(new Reservoire(reservoirSize), queue));
			amount += reservoirSize;
		}
	}
	
	public boolean isInReservoir(){
		boolean enqeued = queue.poll() != null; 
		if (enqeued){
			for (SoftReference<Reservoire> reference : references) {
				reference.enqueue();
			}
			references.clear();
		}
		return enqeued;
	}
	
	protected static class Reservoire{
		protected final byte reserve[]; 
		
		public Reservoire(int size){
			this.reserve = new byte[size];
		}
	}
}
