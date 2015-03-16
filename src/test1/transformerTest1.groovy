package test1

import groovy.json.JsonOutput
import groovy.json.JsonSlurper

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

def capRechargerI = new MaterialType(name: 'Cap Recharger I', typeId: 1195, price: 41700)
def ramET = new MaterialType(name: 'R.A.M.- Energy Tech', typeId: 11482, price: 65.53)

def superconductors = new MaterialType(name: 'Superconductors', typeId: 9838, price: 6710)

def nanotransostors = new MaterialType(name: 'Nanotransistors', typeId: 16681, price: 2420)
def phenolCompos = new MaterialType(name: 'Phenolic Composites', typeId: 16680, price: 1547)
def terahertzMetamaterials = new MaterialType(name: 'Terahertz Metamaterials', typeId: 33360, price: 11836)
def tungstenCarbide = new MaterialType(name: 'Tungsten Carbide', typeId: 16672, price: 118.5)
def fullerides = new MaterialType(name: 'Fullerides', typeId: 16679, price: 600)

def nanoelMicroproc = new MaterialType(name: 'Nanoelectrical Microprocessor', typeId: 11539, price: 63938)
def tesseractCapUnit = new MaterialType(name: 'Tesseract Capacitor Unit', typeId: 11554, price: 36000)

def capRechargerII = new MaterialType(name: 'Cap Recharger II', typeId: 2032, price: 423625)

class MaterialDB {
    def materialByName = [:]
    def materialByType = [:]

    def leftShift(MaterialType type) {
        materialByName[type.name] = type
        materialByType[type.typeId] = type
    }
}

def materialDB = new MaterialDB()
materialDB << tritanium
materialDB << pyerite
materialDB << mexallon
materialDB << isogen
materialDB << nocxium
materialDB << morphite
materialDB << megacyte
materialDB << capRechargerI
materialDB << capRechargerII
materialDB << ramET
materialDB << superconductors
materialDB << nanotransostors
materialDB << phenolCompos
materialDB << terahertzMetamaterials
materialDB << tungstenCarbide
materialDB << fullerides
materialDB << nanoelMicroproc
materialDB << tesseractCapUnit

println materialDB.materialByName
println materialDB.materialByType

def updatePrice(MaterialDB materialDB) {
    def Dodixi = 30002659
    def url = new URL(materialDB.materialByName.values().sum("http://api.eve-central.com/api/marketstat/json?") {MaterialType type -> "typeid=$type.typeId&"} + "usesystem=$Dodixi")
    def marketText = url.text;
    println marketText
    def slurper = new JsonSlurper();
    def result = slurper.parseText(marketText);

    result.each { marketItem ->
        def typeId = marketItem.buy.forQuery.types[0]
        def type = materialDB.materialByType[typeId]
        type.price = marketItem.buy.max
    }

    def jsonOutput = new JsonOutput()
    println jsonOutput.prettyPrint(marketText);
}

updatePrice(materialDB)

ExpandoMetaClass.enableGlobally()
Number.metaClass.unit = {MaterialType type -> new MaterialSlot(type: type, quantity: delegate) }
Number.metaClass.units = {MaterialType type -> new MaterialSlot(type: type, quantity: delegate) }

class TransformerBuilder {
    def Transformer transformer
    def transformers = [:]
    def composite

    def to = [
            produce: {MaterialSlot slot ->
                def bill = new BillOfMaterial()
                bill << slot

                def transformer = new Transformer(name: "$slot.type.name Blueprint", output: bill)
                slot.transformer = transformer
                transformers[slot.type] = transformer
                this.transformer = transformer
//                new TransformerBuilder(transformer: transformer, transformers: transformers)
                this
            }
    ]

    def take(MaterialSlot... list) {
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
        composite << transformer
    }

    def compose(String name, Closure closure) {
        def upperComposite = composite
        composite = new CompositeTransformer(name: name)
        closure.setDelegate(this)
        closure()
        composite.compose()
        def outType = composite.output.iterator().next().type
        transformers[outType] = composite
        if (upperComposite != null) {
            upperComposite << composite
        }
        def newComposite = composite
        composite = upperComposite
        return newComposite
    }
}



def builder = new TransformerBuilder()
def composite = builder.compose("Cap Recharger II Production") {
// to produce 1 unit of 'Cap Recharger II' take 1 unit of 'Cap Recharger I', 2 units of 'R.A.M.- Energy Tech', 3 units of 'Tritanium', 4 units of 'Pyerite', 5 units of 'Mexallon' and 6 units of 'Noxcium'
    compose("Cap Recharger I Production") {
        to.produce 1.unit(capRechargerI) take 1420.units(tritanium), 598.units(pyerite), 767.units(mexallon), 2.units(megacyte)
    }
    to.produce 100.units(ramET) take 529.units(tritanium), 422.units(pyerite), 211.units(mexallon), 78.units(isogen), 35.units(nocxium)
    to.produce 1.unit(nanoelMicroproc) take 2.units(nanotransostors), 6.units(phenolCompos), 2.units(terahertzMetamaterials), 17.units(tungstenCarbide)
    to.produce 1.unit(tesseractCapUnit) take 11.units(fullerides), 1.unit(nanotransostors), 2.units(terahertzMetamaterials), 27.units(tungstenCarbide)
    to.produce 1.unit(capRechargerII) take 1.unit(capRechargerI), 1.unit(ramET), 5.units(superconductors), 3.units(morphite), 1.unit(tesseractCapUnit), 1.unit(nanoelMicroproc)
}

println builder.transformers

println "composite.input=" + composite.input
composite.input.each {slot -> println "$slot.type: $slot.quantity x $slot.type.price = ${slot.type.price * slot.quantity}"}
println "composite.input.total=" + composite.input.sum {slot -> slot.type.price * slot.quantity}
println "composite.output=" + composite.output

