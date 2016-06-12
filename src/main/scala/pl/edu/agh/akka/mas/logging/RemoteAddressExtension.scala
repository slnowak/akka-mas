package pl.edu.agh.akka.mas.logging

import akka.actor.{ExtendedActorSystem, Extension, ExtensionKey}

/**
  * Created by ania on 6/12/16.
  */
class RemoteAddressExtensionImpl(system: ExtendedActorSystem) extends Extension {
  def address = system.provider.getDefaultAddress
}

object RemoteAddressExtension extends ExtensionKey[RemoteAddressExtensionImpl]

