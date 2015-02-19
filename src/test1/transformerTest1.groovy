package test1

/**
 * Created by a.n.vorotnikov on 18.02.2015.
 */

def tritanium = new MaterialType(name: "Tritanium", typeId: 34, price: 6)
def pyerite = new MaterialType(name: "Pyerite", typeId: 35, price: 12.37)
def mexallon = new MaterialType(name: "Mexallon", typeId: 36, price: 58.78)
def isogen = new MaterialType(name: "Isogen", typeId: 37, price: 148.92)
def nocxium = new MaterialType(name: "Noxcium", typeId: 38, price: 704.18)
def morphite = new MaterialType(name: "Morphite", typeId: 39, price: 704.18)
def megacyte = new MaterialType(name: "Megacyte", typeId: 40, price: 894.80)

def capRechargerI = new MaterialType(name: "Cap Recharger I", typeId: 100, price: 40000)

def capRechargerITransformer = new Transformer(name: "Cap Recharger I Blueprint")

def capRechIInputBill = new BillOfMaterial()
capRechIInputBill << new MaterialSlot(type: tritanium, quantity: 1420, transformer: capRechargerITransformer)
capRechIInputBill << new MaterialSlot(type: pyerite, quantity: 598, transformer: capRechargerITransformer)
capRechIInputBill << new MaterialSlot(type: mexallon, quantity: 767, transformer: capRechargerITransformer)
capRechIInputBill << new MaterialSlot(type: megacyte, quantity: 2, transformer: capRechargerITransformer)

capRechargerITransformer.input = capRechIInputBill

def capRechIOutputBill = new BillOfMaterial()
def capRechIOutSlot = new MaterialSlot(type: capRechargerI, quantity: 1, transformer: capRechargerITransformer)
capRechIOutputBill << capRechIOutSlot

capRechargerITransformer.output = capRechIOutputBill

def ramET = new MaterialType(name: "R.A.M.- Energy Tech", typeId: 101, price: 65)

def ramETTransformer = new Transformer(name: "R.A.M.- Energy Tech Blueprint")

def ramETInputBill = new BillOfMaterial()
ramETInputBill << new MaterialSlot(type: tritanium, quantity: 529, transformer: ramETTransformer)
ramETInputBill << new MaterialSlot(type: pyerite, quantity: 422, transformer: ramETTransformer)
ramETInputBill << new MaterialSlot(type: mexallon, quantity: 211, transformer: ramETTransformer)
ramETInputBill << new MaterialSlot(type: isogen, quantity: 78, transformer: ramETTransformer)
ramETInputBill << new MaterialSlot(type: nocxium, quantity: 35, transformer: ramETTransformer)

ramETTransformer.input = ramETInputBill

def ramETOutputBill = new BillOfMaterial()
def ramETOutSlot = new MaterialSlot(type: ramET, quantity: 100, transformer: ramETTransformer)
ramETOutputBill << ramETOutSlot
ramETTransformer.output = ramETOutputBill

def capRechargerII = new MaterialType(name: "Cap Recharger II", typeId: 1000, price: 423625)

def capRechargerIITransformer = new Transformer(name: "Cap Recharger II Blueprint")

def capRechIIInputBill = new BillOfMaterial()
def capRechIICapRechIInputSlot = new MaterialSlot(type: capRechargerI, quantity: 1, transformer: capRechargerIITransformer)
capRechIIInputBill << capRechIICapRechIInputSlot
def capRechIIramETInputSlot = new MaterialSlot(type: ramET, quantity: 1, transformer: capRechargerIITransformer)
capRechIIInputBill << capRechIIramETInputSlot

capRechargerIITransformer.input = capRechIIInputBill

def capRechIIOutputBill = new BillOfMaterial()
capRechIIOutputBill << new MaterialSlot(type: capRechargerII, quantity: 1, transformer: capRechargerIITransformer)

capRechargerIITransformer.output = capRechIIOutputBill

def capRechIToCapRechIILink = new Link(from: capRechIOutSlot, to: capRechIICapRechIInputSlot)
capRechIOutSlot.link = capRechIToCapRechIILink
capRechIICapRechIInputSlot.link = capRechIToCapRechIILink

def ramETToCaprechIILink = new Link(from: ramETOutSlot, to: capRechIIramETInputSlot)
ramETOutSlot.link = ramETToCaprechIILink
capRechIIramETInputSlot.link = ramETToCaprechIILink

def composite = new CompositeTransformer(name: "Cap Recharger II Production")
composite.transformers << capRechargerITransformer
composite.transformers << ramETTransformer
composite.transformers << capRechargerIITransformer

composite.compose()

println "composite.input=" + composite.input
println "composite.input.total=" + composite.input.sum {slot -> slot.type.price * slot.quantity}
println "composite.output=" + composite.output

ExpandoMetaClass.enableGlobally()
Number.metaClass.unit = {type -> new MaterialSlot(type: type, quantity: delegate) }
Number.metaClass.units = {type -> new MaterialSlot(type: type, quantity: delegate) }

def transformers = [:]

class TransformerBuilder {
    def Transformer transformer
    def transformers

    def take(list) {
        def bill = new BillOfMaterial()
        list.each {MaterialSlot slot ->
            bill << slot
            slot.transformer = transformer
            def Transformer out = transformers[slot.type]
            if (out != null) {
                def MaterialSlot fromSlot = out.output.bill[slot.type]
                def link = new Link(from: fromSlot, to: slot)
                fromSlot.link = link
                slot.link = link
            }
        }

        transformer.input = bill
        transformer
    }
}

def to = [
        produce: {MaterialSlot slot ->
            def bill = new BillOfMaterial()
            bill << slot

            def transformer = new Transformer(name: "$slot.type.name Blueprint", output: bill)
            slot.transformer = transformer
            transformers[slot.type] = transformer
            new TransformerBuilder(transformer: transformer, transformers: transformers)
        }
]

// to produce 1 unit of 'Cap Recharger II' take 1 unit of 'Cap Recharger I', 2 units of 'R.A.M.- Energy Tech', 3 units of 'Tritanium', 4 units of 'Pyerite', 5 units of 'Mexallon' and 6 units of 'Noxcium'
println to.produce(1.unit(capRechargerI)).take([1420.units(tritanium), 598.units(pyerite), 767.units(mexallon), 2.units(megacyte)])

println to.produce(1.unit(capRechargerII)).take([1.unit(capRechargerI), 1.unit(ramET)])

println transformers
