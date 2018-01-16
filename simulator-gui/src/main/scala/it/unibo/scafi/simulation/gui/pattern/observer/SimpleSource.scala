package it.unibo.scafi.simulation.gui.pattern.observer

/**
  * the order of notification to observer isn't preserved
  */
trait SimpleSource extends Source {
  private var _observers = Set[O]()
  override def attach(observer : O): Boolean = {
    if (_observers contains observer) return false
    _observers += observer
    return true
  }
  /**
    * remove an observer to the source
    * @param observer want to stop observer this source
    * @return false if the observer currently doesn't observer the source true otherwise
    */
  def detach(observer: O) : Boolean = {
    if (!(_observers contains observer)) return false
    _observers -= observer
    return true
  }
  protected def observers: Iterable[O] = _observers
}