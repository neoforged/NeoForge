var ASMAPI = Java.type('net.neoforged.coremod.api.ASMAPI')

// If you add or remove a new field,
// please also add or remove a corresponding comment in the source code,
// in the interest of modders reading it.
function initializeCoreMod() {
    return {
        'biome': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.biome.Biome' 
            },
            'transformer': function(classNode) {
                ASMAPI.redirectFieldToMethod(classNode, 'climateSettings', 'getModifiedClimateSettings')
                ASMAPI.redirectFieldToMethod(classNode, 'specialEffects', 'getModifiedSpecialEffects')
                return classNode;
            }
        },
        'structure': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.levelgen.structure.Structure'
            },
            'transformer': function(classNode) {
                ASMAPI.redirectFieldToMethod(classNode, 'settings', 'getModifiedStructureSettings')
                return classNode;
            }
        },
        'flowing_fluid_block': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.block.LiquidBlock'
            },
            'transformer': function(classNode) {
                ASMAPI.redirectFieldToMethod(classNode, 'fluid', 'getFluid')
                return classNode;
            }
        },
        'bucketitem': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.item.BucketItem'
            },
            'transformer': function(classNode) {
                ASMAPI.redirectFieldToMethod(classNode, 'content', 'getFluid')
                return classNode;
            }
        },
        'stairsblock': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.block.StairBlock'
            },
            'transformer': function(classNode) {
                ASMAPI.redirectFieldToMethod(classNode, 'base', 'getModelBlock')
                return classNode;
            }
        },
        'flowerpotblock': {
            'target': {
                'type': 'CLASS',
                'name': 'net.minecraft.world.level.block.FlowerPotBlock'
            },
            'transformer': function(classNode) {
                ASMAPI.redirectFieldToMethod(classNode, 'potted', 'getPotted') // flower
                return classNode;
            }
        }
    }
}
