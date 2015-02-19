package test1

/**
 * Created by a.n.vorotnikov on 17.02.2015.
 */
class MaterialType {
    String name
    int typeId
    double price

    def String toString() {
        name
    }
}

class MaterialSlot {
    MaterialType type
    double quantity

    Transformer transformer
    Link link

    def String toString() {
        (type as String) + ':' + quantity
    }
}

class Link {
    MaterialSlot from
    MaterialSlot to
}

class BillOfMaterial implements Iterable {
    def bill = [:]

    def leftShift(MaterialSlot slot) {
        MaterialSlot billSlot = (MaterialSlot) bill.get(slot.type)

        if (billSlot == null) {
//            billSlot = new MaterialSlot(type: slot.type, quantity: 0)
            billSlot = slot
            bill[slot.type] = billSlot
        } else {
            billSlot.quantity += slot.quantity
        }
    }

    def Iterator iterator() {
        bill.values().iterator()
    }

    def String toString() {
        bill.toString()
    }
}

class Transformer {
    String name

    BillOfMaterial input
    BillOfMaterial output

    def String toString() {
        "[$name:$input->$output]"
    }
}

class CompositeTransformer extends Transformer {
    def transformers = []

    private Transformer findTerminalTransformer() {
        transformers.find {
            transformer -> transformer.output.every { slot -> slot.link == null }
        }
    }

    private compose0(Transformer transformer, double coef) {
        transformer.output.each {
            slot ->
                if (slot.link == null) {
                    output << new MaterialSlot(type: slot.type, quantity: slot.quantity * coef)
                }
        }

        transformer.input.each {
            MaterialSlot slot ->
                if (slot.link == null) {
                    input << new MaterialSlot(type: slot.type, quantity: slot.quantity * coef)
                } else {
                    def coef1 = coef * slot.quantity / slot.link.from.quantity
                    compose0(slot.link.from.transformer, coef1)
                }
        }
    }

    def compose() {
        input = new BillOfMaterial()
        output = new BillOfMaterial()
        def terminalTransformer = findTerminalTransformer();

        compose0(terminalTransformer, 1)
    }
}
